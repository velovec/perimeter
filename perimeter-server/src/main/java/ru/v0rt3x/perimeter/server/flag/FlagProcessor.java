package ru.v0rt3x.perimeter.server.flag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
    private FlagQueue flagQueue;

    @Autowired
    private FlagStats flagStats;

    private ContestState contestState = ContestState.INITIAL;
    private Integer contestRound = 0;

    private static final Logger logger = LoggerFactory.getLogger(FlagProcessor.class);

    @PostConstruct
    private void initQueue() {
        final int[] stats = new int[] { 0, 0, 0, 0, 0 };

        List<Flag> flags = flagRepository.findAllByOrderByCreateTimeStampDesc();

        flags.stream()
            .filter(flag -> flag.getStatus() == QUEUED)
            .peek(flag -> stats[flag.getPriority().ordinal()]++)
            .forEach(flagQueue::enqueueFlag);

        flags.stream()
            .filter(flag -> flag.getStatus() != QUEUED)
            .peek(flag -> stats[flag.getStatus().ordinal() + 2]++)
            .forEach(flagQueue::addToHistory);

        flagStats.setStats(stats);
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

    @Scheduled(fixedRate = 5000L)
    private void processQueue() {
        List<Flag> flagsToSend = new ArrayList<>();
        List<Flag> flagsToUpdate = new ArrayList<>();

        while (flagsToSend.size() < 25) {
            Flag flag = flagQueue.pollFlag();

            if (flag == null)
                break;

            if (flag.getCreateTimeStamp() + perimeterProperties.getFlag().getTtl() * 1000 <= System.currentTimeMillis()) {
                flag.setStatus(REJECTED);

                flagStats.incrementByStatus(flag.getStatus());
                flagStats.decrementByPriority(flag.getPriority());

                flagsToUpdate.add(flag);
                flagQueue.addToHistory(flag);

                continue;
            }

            flagsToSend.add(flag);
        }

        if (flagsToUpdate.size() > 0) {
            flagRepository.saveAll(flagsToUpdate);
        }

        if (flagsToSend.size() > 0) {
            List<FlagResult> results = themisClient.sendFlags(flagsToSend);
            processResults(flagsToSend, results);
        }
    }

    private void processResults(List<Flag> flags, List<FlagResult> results) {
        int resultsCount = results.size();
        int flagsCount = flags.size();

        if (resultsCount != flagsCount) {
            if (resultsCount == 1) {
                logger.warn("ThemisError: {}", results.get(0));

                flags.parallelStream()
                    .peek(flag -> flagStats.decrementByPriority(flag.getPriority()))
                    .forEach(flagQueue::enqueueFlag);
            } else {
                logger.error("Invalid Themis response: Got {} results but expected {}", resultsCount, flagsCount);
            }
        } else {
            for (int id = 0; id < resultsCount; id++) {
                Flag flag = flags.get(id);
                FlagResult result = results.get(id);

                switch (result) {
                    case FLAG_ACCEPTED:
                    case FLAG_EXPIRED:
                    case FLAG_BELONGS_ATTACKER:
                    case FLAG_ALREADY_ACCEPTED:
                    case FLAG_NOT_FOUND:
                        flag.setStatusByResult(result);

                        flagStats.decrementByPriority(flag.getPriority());
                        flagStats.incrementByStatus(flag.getStatus());

                        flagRepository.save(flag);
                        flagQueue.addToHistory(flag);
                        break;
                    case CONTEST_NOT_STARTED:
                    case CONTEST_PAUSED:
                    case CONTEST_COMPLETED:
                    case LIMIT_EXCEEDED:
                    case SERVICE_IS_DOWN:
                        logger.warn("ThemisError: {} : {}", flag, result);
                        flagStats.decrementByPriority(flag.getPriority());
                        flagQueue.enqueueFlag(flag);
                        break;
                    default:
                        logger.warn("Unexpected result: {} : {}", flag, result);
                        break;
                }
            }
        }
    }
}
