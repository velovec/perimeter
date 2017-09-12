package ru.v0rt3x.perimeter.server.web.views.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/flag")
public class FlagRESTController {

    @Autowired
    private FlagQueue flagQueue;

    @Autowired
    private FlagRepository flagRepository;

    @MessageMapping("/flag/send")
    public void putFlag(Flag flag) {
        putFlag("high", Collections.singletonList(flag));
    }

    @RequestMapping(path = "/{queue}/send", method = RequestMethod.POST)
    public int putFlag(@PathVariable String queue, @RequestBody List<Flag> flags) {
        return flags.stream()
            .peek(flag -> flag.setPriority(FlagPriority.valueOf(queue.toUpperCase())))
            .peek(flag -> flag.setCreateTimeStamp(System.currentTimeMillis()))
            .peek(flag -> flag.setStatus(FlagStatus.QUEUED))
            .map(flagQueue::enqueueFlag)
            .mapToInt(x -> x ? 1 : 0)
            .sum();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Flag> getLastFlags(@RequestParam(required = false) Integer limit) {
        List<Flag> lastFlags = flagRepository.findAllByOrderByLastUpdateTimeStampDesc();
        return lastFlags.subList(0, Math.min(limit != null ? limit : 10, lastFlags.size()));
    }
}
