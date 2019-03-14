package ru.v0rt3x.perimeter.server.flag.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flag")
public class FlagRESTController {

    @Autowired
    private FlagProcessor flagProcessor;

    @RequestMapping(path = "/{queue}/send", method = RequestMethod.POST)
    public int putFlag(@PathVariable String queue, @RequestBody List<Flag> flags) {
        return flags.stream()
            .peek(flag -> flag.setPriority(FlagPriority.valueOf(queue.toUpperCase())))
            .peek(flag -> flag.setCreateTimeStamp(System.currentTimeMillis()))
            .peek(flag -> flag.setStatus(FlagStatus.QUEUED))
            .map(flagProcessor::addFlag)
            .mapToInt(x -> x ? 1 : 0)
            .sum();
    }

    @RequestMapping(path = "/stats", method = RequestMethod.GET)
    public Map<String, Object> getFlagStats() {
        return flagProcessor.getStats();
    }
}
