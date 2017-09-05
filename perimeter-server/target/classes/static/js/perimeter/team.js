eventHandlers = {
    update_team: onTeamUpdate,
    delete_team: onTeamDelete,
    sync_team: onTeamSync
};

var team_sync_table = $("#team_sync");
var team_sync_data = null;

var team_table = $("#teams");

function onTeamUpdate(event) {
    onTeamDelete(event);

    var team = $("<tr></tr>").attr("id", "team_id" + event.data.id);

    team.append($("<td></td>").text(event.data.id));
    team.append($("<td></td>").text(event.data.name));
    team.append($("<td></td>").text(event.data.ip));
    team.append($("<td></td>").append(
        $("<i></i>").attr("class", "material-icons text-" + (event.data.guest ? "success" : "danger")).text(event.data.guest ? "done" : "clear")
    ).append(
        $("<div></div>").attr("class", "ripple-container")
    ));
    team.append($("<td></td>").append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-danger btn-round btn-just-icon').click(function () { deleteTeam(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("delete")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ));

    team_table.append(team);
}

function onTeamDelete(event) {
    var team_element = $("#team_id" + event.data.id);

    if (team_element.length >= 0) {
        team_element.remove();
    }
}

function onTeamSync(event) {
    var teams = event.data;

    team_sync_table.empty();
    team_sync_data = teams;

    for (var i = 0; i < teams.length; i++) {
        var team = $("<tr></tr>").attr("id", "team_sync_id" + teams[i].id);

        team.append($("<td></td>").text(teams[i].id));
        team.append($("<td></td>").text(teams[i].name));
        team.append($("<td></td>").text(teams[i].ip));
        team.append($("<td></td>").append(
            $("<i></i>").attr("class", "material-icons text-" + (teams[i].guest ? "success" : "danger")).text(teams[i].guest ? "done" : "clear")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        ));


        team_sync_table.append(team);
    }
}

function requestTeamSync() {
    stompClient.send("/ws/team/request_sync", {}, null);
}

function confirmTeamSync() {
    if (team_sync_data) {
        stompClient.send("/ws/team/confirm_sync", {}, JSON.stringify(team_sync_data));
        console.log(JSON.stringify(team_sync_data));
        notify("success", "Team synchronization started");
    } else {
        notify("warning", "No data to sync");
    }
}