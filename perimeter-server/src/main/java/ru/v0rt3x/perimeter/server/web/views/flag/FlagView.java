package ru.v0rt3x.perimeter.server.web.views.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import javax.annotation.PostConstruct;
import java.util.*;

import static ru.v0rt3x.perimeter.server.web.views.flag.FlagPriority.*;
import static ru.v0rt3x.perimeter.server.web.views.flag.FlagStatus.*;

@Controller
@UIView(name = "flag", linkOrder = 2, link = "/flag/", title = "Flag Processor", icon = "flag")
public class FlagView extends UIBaseView {

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

    @ModelAttribute("FLAG_STATS")
    public FlagStats getFlagStats() {
        return flagStats;
    }

    @ModelAttribute("FLAGS")
    private List<Flag> getFlags() {
        return flagRepository.findAllByOrderByLastUpdateTimeStampDesc(new PageRequest(0, 10)).getContent();
    }

    @ModelAttribute("CONTEST_STATE")
    public ContestState getContestState() {
        return contestState;
    }

    @ModelAttribute("CONTEST_ROUND")
    public Integer getContestRound() {
        return contestRound;
    }

    @RequestMapping(value = "/flag/", method = RequestMethod.GET)
    private String index(Map<String, Object> context) {
        return "flag";
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

                eventProducer.notify("contest_state", new Object[]{contestState, contestRound});
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

                getFlagStats().incrementByStatus(flag.getStatus());
                getFlagStats().decrementByPriority(flag.getPriority());

                flagsToUpdate.add(flag);
                flagQueue.addToHistory(flag);

                continue;
            }

            flagsToSend.add(flag);
        }

        if (flagsToUpdate.size() > 0) {
            eventProducer.saveAndNotify(flagRepository, flagsToUpdate);
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

                        eventProducer.saveAndNotify(flagRepository, flag);
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
