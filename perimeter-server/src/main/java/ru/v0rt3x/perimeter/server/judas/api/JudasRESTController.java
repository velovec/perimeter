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
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.team.TeamManager;

import java.util.Objects;

@RestController
@RequestMapping("/api/judas")
public class JudasRESTController {

    @Autowired
    private FlagProcessor flagProcessor;

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private TeamManager teamManager;

    @RequestMapping(path = "/target/{port:[0-9]+}/", method = RequestMethod.GET)
    public JudasTarget getTarget(@PathVariable("port") int port) {
        Service service = serviceManager.getService(port);

        if (Objects.isNull(service))
            return null;

        if (!service.getMode().equals("http"))
            return null;

        JudasTarget target = new JudasTarget();

        target.setProtocol("http");
        target.setHost(teamManager.getRandomTeam().getIp());
        target.setPort(service.getPort());

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
