package ru.v0rt3x.perimeter.server.flag;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.utils.PEMUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.regex.Matcher;

import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.HIGH;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.LOW;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.NORMAL;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagStatus.QUEUED;

@Component
public class FlagQueue {

    private final Map<FlagPriority, Queue<Flag>> flagQueue = new HashMap<>();
    private final Set<Flag> flagHistory = new HashSet<>();

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private FlagStats flagStats;

    private Algorithm algorithm = null;
    private JWTVerifier verifier = null;

    private static final Logger logger = LoggerFactory.getLogger(FlagQueue.class);

    @PostConstruct
    private void initQueue() {
        flagQueue.put(LOW, new LinkedList<>());
        flagQueue.put(NORMAL, new LinkedList<>());
        flagQueue.put(HIGH, new LinkedList<>());
    }

    public boolean enqueueFlag(Flag flag) {
        if (!isReady()) {
            logger.warn("FlagQueue is not ready to process flags");
            return false;
        }

        if (perimeterProperties.getFlag().getJwt().isEnabled()) {
            Matcher jwtMatcher = perimeterProperties.getFlag().getJwt().getPattern().matcher(flag.getFlag());
            if (!jwtMatcher.matches()) {
                return false;
            }
            flag.setFlag(jwtMatcher.group("flag"));

            try {
                DecodedJWT token = verifier.verify(flag.getFlag());

                flag.setFlag(token.getClaim("flag").asString());
            } catch (JWTVerificationException e) {
                logger.debug("Unable to verify token: {}", e.getMessage());
                return false;
            }
        }

        Matcher flagMatcher = perimeterProperties.getFlag().getPattern().matcher(flag.getFlag());
        if (flagMatcher.matches()) {
            flag.setFlag(flagMatcher.group("flag"));

            if (flag.getStatus() != QUEUED)
                return false;

            if (flagHistory.contains(flag))
                return false;

            synchronized (flagQueue) {
                flagRepository.save(flag);

                flagQueue.get(flag.getPriority()).add(flag);
                flagStats.incrementByPriority(flag.getPriority());
            }

            return true;
        } else {
            return false;
        }
    }

    Flag pollFlag() {
        for (FlagPriority priority : new FlagPriority[] { HIGH, NORMAL, LOW }) {
            if (!flagQueue.get(priority).isEmpty()) {
                synchronized (flagQueue) {
                    return flagQueue.get(priority).poll();
                }
            }
        }

        return null;
    }

    void addToHistory(Flag flag) {
        flagHistory.add(flag);
    }

    public boolean isReady() {
        return !perimeterProperties.getFlag().getJwt().isEnabled() ||
            Objects.nonNull(verifier);
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
}
