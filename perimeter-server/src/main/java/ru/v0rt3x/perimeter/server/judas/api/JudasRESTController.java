package ru.v0rt3x.perimeter.server.judas.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagStatus;
import ru.v0rt3x.perimeter.server.judas.JudasConfig;
import ru.v0rt3x.perimeter.server.judas.JudasTarget;

import java.util.Objects;

@RestController
@RequestMapping("/api/judas")
public class JudasRESTController {

    @Autowired
    private FlagProcessor flagProcessor;

    @RequestMapping(path = "/target/{port:[0-9]+}/", method = RequestMethod.GET)
    public JudasTarget getTarget(@PathVariable("port") int port) {
        JudasTarget target = new JudasTarget();

        // TODO: Make it choose random / specific team
        // TODO: Remove hardcode
        target.setProtocol("http");
        target.setHost("10.20.30.1");
        target.setPort(5000);

        return target;
    }

    @RequestMapping(path = "/config/", method = RequestMethod.GET)
    public JudasConfig getConfig() {
        JudasConfig config = new JudasConfig();

        config.setPattern(flagProcessor.getFlagPattern().pattern());

        return config;
    }

    @RequestMapping(path = "/submit/", method = RequestMethod.POST)
    public void submitFlag(@RequestBody Flag flag) {
        if (Objects.nonNull(flag) && Objects.nonNull(flag.getFlag()) && (flag.getFlag().length() > 0)) {
            flag.setCreateTimeStamp(System.currentTimeMillis());

            flag.setStatus(FlagStatus.QUEUED);
            flag.setPriority(FlagPriority.NORMAL);

            flagProcessor.addFlag(flag);
        }
    }
}
