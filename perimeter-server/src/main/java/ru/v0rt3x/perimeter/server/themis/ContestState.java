package ru.v0rt3x.perimeter.server.themis;

public enum ContestState {
    /*
     * Source: https://github.com/themis-project/themis-finals-backend/blob/master/lib/constants/competition_stage.rb
     */
    INITIAL, AWAIT_START, RUNNING, AWAIT_PAUSE,
    PAUSED, AWAIT_COMPLETE, COMPLETED, NOT_AVAILABLE
}
