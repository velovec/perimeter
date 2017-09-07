eventHandlers = {
    stats_flag: onFlagStats,
    contest_state: onContestStateChange
};

var contest_state = $("#contest_state");
var contest_state_icon = $("#contest_state_icon");
var contest_round = $("#contest_round");

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

var state_to_status = {
    INITIAL: { color: "blue", icon: "autorenew", message: "Contest is initialized" },
    AWAIT_START: { color: "blue", icon: "autorenew", message: "Contest is starting" },
    RUNNING: { color: "green", icon: "play_arrow", message: "Contest is running" },
    PAUSED: { color: "orange", icon: "pause", message: "Contest is paused" },
    AWAIT_COMPLETE: { color: "red", icon: "stop", message: "Contest is preparing to complete" },
    COMPLETED: { color: "red", icon: "stop", message: "Contest is completed" },
    NOT_AVAILABLE: { color: "red", icon: "error_outline", message: "Themis integration is disabled" }
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

function onContestStateChange(event) {
    contest_round.text(event.data[1]);

    contest_state_icon.parent().attr("data-background-color", state_to_status[event.data[0]].color);
    contest_state_icon.text(state_to_status[event.data[0]].icon);
    contest_state.text(state_to_status[event.data[0]].message);
}