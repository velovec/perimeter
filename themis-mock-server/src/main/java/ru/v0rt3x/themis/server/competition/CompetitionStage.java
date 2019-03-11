package ru.v0rt3x.themis.server.competition;

public enum CompetitionStage {
    /*
     * Source: https://github.com/themis-project/themis-finals-backend/blob/master/lib/constants/competition_stage.rb
     */
    NOT_STARTED, STARTING, STARTED,
    PAUSING, PAUSED, FINISHING, FINISHED
}
