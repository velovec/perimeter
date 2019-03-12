package ru.v0rt3x.perimeter.server.flag;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.shell.console.Table;
import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.utils.PEMUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.*;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagStatus.*;

@Component
public class FlagProcessor {

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private ThemisClient themisClient;

    private Algorithm algorithm = null;
    private JWTVerifier verifier = null;

    private ContestState contestState = ContestState.INITIAL;
    private Integer contestRound = 0;

    private final Set<Flag> flagHistory = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(FlagProcessor.class);

    @PostConstruct
    private void initQueue() {
        if (perimeterProperties.getFlag().getJwt().isEnabled()) {
            setThemisPublicKey(themisClient.getPublicKey());
        }

        flagHistory.addAll(flagRepository.findAllByStatusOrderByCreateTimeStampDesc(ACCEPTED));
        flagHistory.addAll(flagRepository.findAllByStatusOrderByCreateTimeStampDesc(REJECTED));
    }

    public boolean addFlag(Flag flag) {
        flag = decodeFlag(flag);
        if (Objects.isNull(flag)) {
            logger.debug("Flag is null");
            return false;
        }

        Matcher flagMatcher = perimeterProperties.getFlag().getPattern().matcher(flag.getFlag());
        if (flagMatcher.matches()) {
            flag.setFlag(flagMatcher.group("flag"));

            if (flag.getStatus() != QUEUED) {
                logger.debug("Flag not in QUEUED status: ({}) {}", flag.getStatus(), flag.getFlag());
                return false;
            }

            if (flagHistory.contains(flag)) {
                logger.debug("Flag already in history: {}", flag.getFlag());
                return false;
            }

            flagRepository.save(flag);
            return true;
        } else {
            logger.debug("Flag doesn't match pattern: {}", flag.getFlag());
            return false;
        }
    }

    private Flag decodeFlag(Flag flag) {
        Matcher flagMatcher = perimeterProperties.getFlag().getPattern().matcher(flag.getFlag());
        if (flagMatcher.matches()) {
            flag.setFlag(flagMatcher.group("flag"));
            return flag;
        }

        if (!perimeterProperties.getFlag().getJwt().isEnabled())
            return null;

        if (!isReady()) {
            logger.warn("JWT is not ready to decode flags");
            return null;
        }

        Matcher jwtMatcher = perimeterProperties.getFlag().getJwt().getPattern().matcher(flag.getFlag());
        if (!jwtMatcher.matches()) {
            return null;
        }
        flag.setFlag(jwtMatcher.group("flag"));

        try {
            DecodedJWT token = verifier.verify(flag.getFlag());

            flag.setFlag(token.getClaim("flag").asString());
        } catch (JWTVerificationException e) {
            logger.debug("Unable to verify token: {}", e.getMessage());
            return null;
        }

        return flag;
    }


    public boolean isReady() {
        return !perimeterProperties.getFlag().getJwt().isEnabled() ||
            Objects.nonNull(verifier);
    }

    @Scheduled(fixedRate = 10000L)
    private void watchContestStatus() {
        if (perimeterProperties.getThemis().isIntegrationEnabled()) {
            ContestState currentState = themisClient.getContestState();
            Integer currentRound = themisClient.getContestRound();

            currentRound = currentRound != null ? currentRound : 0;

            if (!contestState.equals(currentState) || !contestRound.equals(currentRound)) {
                contestState = currentState;
                contestRound = currentRound;
            }
        } else {
            contestState = ContestState.NOT_AVAILABLE;
            contestRound = 0;
        }
    }

    Flag getFlag() {
        for (FlagPriority priority : new FlagPriority[] { HIGH, NORMAL, LOW }) {
            Flag flag = flagRepository.findFirstByStatusAndPriorityOrderByCreateTimeStamp(QUEUED, priority);
            if (Objects.nonNull(flag))
                return flag;
        }

        return null;
    }

    @Scheduled(fixedRate = 5000L)
    private void processQueue() {
        if (contestState.equals(ContestState.PAUSED) || contestState.equals(ContestState.COMPLETED))
            return;

        while (true) {
            Flag flag = getFlag();

            if (flag == null)
                break;

            if (flag.getCreateTimeStamp() + perimeterProperties.getFlag().getTtl() * 1000 <= System.currentTimeMillis()) {
                flag.setStatus(REJECTED);
                logger.debug("Flag rejected: {}", flag.getFlag());
                flagHistory.add(flag);
                flagRepository.save(flag);
                continue;
            }

            FlagInfo flagInfo = themisClient.getFlagInfo(flag);
            if (!flagInfo.isValid() || flagInfo.isExpired()) {
                flag.setStatus(REJECTED);
                logger.debug("Flag rejected by Themis: {}", flag.getFlag());
                flagHistory.add(flag);
                flagRepository.save(flag);
                continue;
            }

            FlagResult result = themisClient.submitFlag(flag);
            logger.debug("Flag processed: {}", flag.getFlag());
            if (!processResults(flag, result)) {
                break;
            }
        }
    }

    private boolean processResults(Flag flag, FlagResult result) {
        switch (result) {
            case FLAG_ACCEPTED:
            case FLAG_EXPIRED:
            case FLAG_BELONGS_ATTACKER:
            case FLAG_ALREADY_ACCEPTED:
            case FLAG_NOT_FOUND:
                flag.setStatusByResult(result);

                flagRepository.save(flag);
                flagHistory.add(flag);
                return true;
            case SERVICE_IS_DOWN:
                return true;
            case CONTEST_NOT_STARTED:
            case CONTEST_PAUSED:
            case CONTEST_COMPLETED:
            case LIMIT_EXCEEDED:
                logger.warn("ThemisError: {} : {}", flag, result);
                return false;
            default:
                logger.error("Unexpected result: {} : {}", flag, result);
                return false;
        }
    }

    public void setThemisPublicKey(String publicKey) {
        try {
            switch (perimeterProperties.getFlag().getJwt().getAlgorithm()) {
                case "EC256":
                    algorithm = Algorithm.ECDSA256(
                        (ECPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "EC"), null
                    );
                    break;
                case "EC384":
                    algorithm = Algorithm.ECDSA384(
                        (ECPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "EC"), null
                    );
                    break;
                case "EC512":
                    algorithm = Algorithm.ECDSA512(
                        (ECPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "EC"), null
                    );
                    break;
                case "RSA256":
                    algorithm = Algorithm.RSA256(
                        (RSAPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "RSA"), null
                    );
                    break;
                case "RSA384":
                    algorithm = Algorithm.RSA384(
                        (RSAPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "RSA"), null
                    );
                    break;
                case "RSA512":
                    algorithm = Algorithm.RSA512(
                        (RSAPublicKey) PEMUtils.readPublicKeyFromString(publicKey, "RSA"), null
                    );
                    break;
            }

            verifier = JWT.require(algorithm).build();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to setup JWT: %s", e.getMessage()));
        }
    }

    public void clearQueue() {
        flagHistory.clear();
        flagRepository.deleteAll();
    }

    public Table getStatsAsTable() {
        Table stats = new Table("Queued (HIGH)", "Queued (NORMAL)", "Queued (LOW)", "Accepted", "Rejected");

        stats.addRow(
            flagRepository.countAllByStatusAndPriority(QUEUED, HIGH),
            flagRepository.countAllByStatusAndPriority(QUEUED, NORMAL),
            flagRepository.countAllByStatusAndPriority(QUEUED, LOW),
            flagRepository.countAllByStatus(ACCEPTED),
            flagRepository.countAllByStatus(REJECTED)
        );

        return stats;
    }
}
