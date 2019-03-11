package ru.v0rt3x.themis.server.competition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/competition")
public class CompetitionController {

    @Autowired
    private CompetitionManager competitionManager;

    @RequestMapping(path = "/api/competition/round", method = RequestMethod.GET)
    public Integer getRound() {
        return competitionManager.getRound();
    }

    @RequestMapping(path = "/api/competition/stage", method = RequestMethod.GET)
    public Integer getStage() {
        return competitionManager.getStage().ordinal();
    }
}
