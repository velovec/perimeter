package ru.v0rt3x.themis.server.competition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.themis.server.properties.ThemisProperties;

import javax.annotation.PostConstruct;

@Component
public class CompetitionManager {

    @Autowired
    private ThemisProperties themisProperties;

    private int round = 0;
    private CompetitionStage stage = CompetitionStage.NOT_STARTED;
    private Long startTime;

    private static final Long ROUND_DURATION = 20000L;

    @PostConstruct
    public void setUpCompetition() {
        stage = CompetitionStage.STARTED;
        startTime = System.currentTimeMillis();
    }

    public Integer getRound() {
        if (stage == CompetitionStage.STARTED) {
            round = Math.toIntExact((System.currentTimeMillis() - startTime) / ROUND_DURATION);
        }

        return round;
    }

    public CompetitionStage getStage() {
        if (System.currentTimeMillis() >= startTime + themisProperties.getDuration())
            stage = CompetitionStage.FINISHED;

        return stage;
    }
}
