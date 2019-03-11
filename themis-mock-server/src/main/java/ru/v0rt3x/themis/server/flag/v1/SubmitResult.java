package ru.v0rt3x.themis.server.flag.v1;

public enum SubmitResult {
    /*
     * Source: https://github.com/themis-project/themis-finals-backend/blob/master/lib/constants/submit_result.rb
     */
    SUCCESS, ERROR_UNKNOWN, ERROR_ACCESS_DENIED, ERROR_COMPETITION_NOT_STARTED,
    ERROR_COMPETITION_PAUSED, ERROR_COMPETITION_FINISHED, ERROR_FLAG_INVALID,
    ERROR_RATELIMIT, ERROR_FLAG_EXPIRED, ERROR_FLAG_YOUR_OWN, ERROR_FLAG_SUBMITTED,
    ERROR_FLAG_NOT_FOUND, ERROR_SERVICE_STATE_INVALID
}
