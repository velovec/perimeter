package ru.v0rt3x.perimeter.server.flag.dao;

public enum FlagResult {
    /*
     * Source: https://github.com/themis-project/themis-finals-attack-result-rb/blob/master/lib/themis/finals/attack/result.rb
     */
    FLAG_ACCEPTED,
    GENERIC_ERROR,
    NOT_A_TEAM,
    CONTEST_NOT_STARTED,
    CONTEST_PAUSED,
    CONTEST_COMPLETED,
    INVALID_FORMAT,
    LIMIT_EXCEEDED,
    FLAG_EXPIRED,
    FLAG_BELONGS_ATTACKER,
    FLAG_ALREADY_ACCEPTED,
    FLAG_NOT_FOUND,
    SERVICE_IS_DOWN
}
