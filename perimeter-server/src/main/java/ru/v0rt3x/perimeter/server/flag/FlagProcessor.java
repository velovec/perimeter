package ru.v0rt3x.perimeter.server.flag;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.shell.console.Table;
import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.utils.PEMUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    private EventManager eventManager;

    private Algorithm algorithm = null;
    private JWTVerifier verifier = null;

    private ContestState contestState = ContestState.INITIAL;
    private Integer contestRound = 0;

    private final Set<Flag> flagHistory = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(FlagProcessor.class);

    @PostConstruct
    private void initQueue() {
        if (perimeterProperties.getThemis().isIntegrationEnabled() && perimeterProperties.getFlag().getJwt().isEnabled()) {
            setThemisPublicKey(themisClient.getPublicKey());
        } else {
            setThemisPublicKey(perimeterProperties.getThemis().getPublicKey());
        }

        flagHistory.addAll(flagRepository.findAllByStatusOrderByCreateTimeStampDesc(ACCEPTED));
        flagHistory.addAll(flagRepository.findAllByStatusOrderByCreateTimeStampDesc(REJECTED));
    }

    @PostConstruct
    private void setUpMetrics() {
        Gauge.builder("flag_processor_flags_in_queue", flagRepository, (r) -> (double) r.countAllByStatus(ACCEPTED))
            .tag("status", "accepted")
            .tag("priority", "unknown")
            .register(Metrics.globalRegistry);

        Gauge.builder("flag_processor_flags_in_queue", flagRepository, (r) -> (double) r.countAllByStatus(REJECTED))
            .tag("status", "rejected")
            .tag("priority", "unknown")
            .register(Metrics.globalRegistry);

        Gauge.builder("flag_processor_flags_in_queue", flagRepository, (r) -> (double) r.countAllByStatusAndPriority(QUEUED, HIGH))
            .tag("status", "queued")
            .tag("priority", "high")
            .register(Metrics.globalRegistry);

        Gauge.builder("flag_processor_flags_in_queue", flagRepository, (r) -> (double) r.countAllByStatusAndPriority(QUEUED, NORMAL))
            .tag("status", "queued")
            .tag("priority", "normal")
            .register(Metrics.globalRegistry);

        Gauge.builder("flag_processor_flags_in_queue", flagRepository, (r) -> (double) r.countAllByStatusAndPriority(QUEUED, LOW))
            .tag("status", "queued")
            .tag("priority", "low")
            .register(Metrics.globalRegistry);
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

    Queue<Flag> getFlags(int flagsToCollect) {
        Queue<Flag> flags = new LinkedList<>();

        for (FlagPriority priority : new FlagPriority[] { HIGH, NORMAL, LOW }) {
            List<Flag> flagList = flagRepository.findAllByStatusAndPriorityOrderByCreateTimeStampDesc(QUEUED, priority, PageRequest.of(0, flagsToCollect));

            flagsToCollect -= flagList.size();

            flags.addAll(flagList);

            if (flagsToCollect == 0)
                break;
        }

        return flags;
    }

    @Scheduled(fixedRate = 5000L)
    private void processQueue() {
        if (contestState.equals(ContestState.PAUSED) || contestState.equals(ContestState.COMPLETED))
            return;

        Queue<Flag> flags = getFlags(200);
        while (true) {
            Flag flag = flags.poll();

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
            if (Objects.isNull(flagInfo) || !flagInfo.isValid() || flagInfo.isExpired()) {
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

    @Scheduled(fixedRate = 5000L)
    public void checkFlagsExpiration() {
        long createdBefore = System.currentTimeMillis() - perimeterProperties.getFlag().getTtl() * 1000;

        int count = 0;
        for (Flag flag: flagRepository.findAllByStatusAndCreateTimeStampLessThan(QUEUED, createdBefore)) {
            flag.setStatus(REJECTED);
            flagHistory.add(flag);
            flagRepository.save(flag);
            count++;
        }

        if (count > 0) {
            eventManager.createEvent(EventType.INFO, "%d flags expired", count);
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
                eventManager.createEvent(EventType.URGENT, "Unexpected response from Themis: %s", result);
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
            eventManager.createEvent(EventType.INFO, "Themis public key updated");
        } catch (IOException e) {
            eventManager.createEvent(EventType.URGENT, "Unable to setup JWT: %s", e.getMessage());
            throw new IllegalStateException(String.format("Unable to setup JWT: %s", e.getMessage()));
        }
    }

    public void clearQueue() {
        flagHistory.clear();
        flagRepository.deleteAll();
        eventManager.createEvent(EventType.INFO, "Flag queue was cleared");
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

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Object> queued = new HashMap<>();

        queued.put("low", flagRepository.countAllByStatusAndPriority(QUEUED, LOW));
        queued.put("normal", flagRepository.countAllByStatusAndPriority(QUEUED, NORMAL));
        queued.put("high", flagRepository.countAllByStatusAndPriority(QUEUED, HIGH));

        stats.put("queued", queued);
        stats.put("accepted", flagRepository.countAllByStatus(ACCEPTED));
        stats.put("rejected", flagRepository.countAllByStatus(REJECTED));

        return stats;
    }

    public Pattern getFlagPattern() {
        PerimeterProperties.FlagProperties flagProperties = perimeterProperties.getFlag();

        return flagProperties.getJwt().isEnabled() ? flagProperties.getJwt().getPattern() : flagProperties.getPattern();
    }
}
