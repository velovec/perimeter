eventHandlers = {
    update_flag: onFlagUpdate,
    stats_flag: onFlagStats,
    contest_state: onContestStateChange
};

var state_map = {
    ACCEPTED: "text-success",
    REJECTED: "text-danger",
    QUEUED: "text-warning"
};

var flags = $("#flags");
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

var state_to_icon = {
    INITIAL: { color: "blue", icon: "autorenew" },
    AWAIT_START: { color: "blue", icon: "autorenew" },
    RUNNING: { color: "green", icon: "play_arrow" },
    PAUSED: { color: "orange", icon: "pause" },
    AWAIT_COMPLETE: { color: "red", icon: "stop" },
    COMPLETED: { color: "red", icon: "stop" }
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

function onFlagUpdate(event) {
    var flag = event.data;

    var flag_selector = $("#flag_id" + flag.id);
    if (flag_selector.length > 0) {
        flag_selector.remove();
    }

    var flag_element = $("<tr></tr>").attr("id", "flag_id" + flag.id);

    flag_element.append($("<td></td>").attr("class", "text-muted").text(flag.flag));
    flag_element.append($("<td></td>").text(flag.priority));
    flag_element.append($("<td></td>").attr("class", state_map[flag.status]).text(flag.status));
    flag_element.append($("<td></td>").text(flag.createTimeStamp));

    flags.prepend(flag_element);
}

function onContestStateChange(event) {
    contest_round.text(event.data[1]);

    contest_state_icon.parent().attr("data-background-color", state_to_icon[event.data[0]].color);
    contest_state_icon.text(state_to_icon[event.data[0]].icon);
    contest_state.text("Contest is " + event.data[0]);
}