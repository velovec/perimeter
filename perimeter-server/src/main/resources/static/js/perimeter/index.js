eventHandlers = {
    stats_flag: onFlagStats
};

var flag_stats = {
    queued: {
        high: $("#flag_stats_qh"),
        normal: $("#flag_stats_qn"),
        low: $("#flag_stats_ql"),
        total: $("#flag_stats_qt")
    },
    accepted: $("#flag_stats_ac"),
    rejected: $("#flag_stats_rj"),
    processing: $("#flag_stats_pr")
};


function onFlagStats(event) {
    var stats = event.data;

    flag_stats.queued.high.text(stats.queued[0]);
    flag_stats.queued.normal.text(stats.queued[1]);
    flag_stats.queued.low.text(stats.queued[2]);
    flag_stats.queued.total.text(stats.queued[0] + stats.queued[1] + stats.queued[2]);

    flag_stats.accepted.text(stats.accepted);
    flag_stats.rejected.text(stats.rejected);
}