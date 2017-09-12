eventHandlers = {
    update_team: onTeamUpdate,
    delete_team: onTeamDelete,
    sync_team: onTeamSync
};

var team_sync_table = $("#team_sync");
var team_sync_data = null;

var new_team_id = $("#new_team_id");
var new_team_name = $("#new_team_name");
var new_team_ip = $("#new_team_ip");

var team_table = $("#teams");

function onTeamUpdate(event) {
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
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-' + (event.data.active ? "success" : "danger") +' btn-round btn-just-icon').click(function () { toggleTeam(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("power_settings_new")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ).append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-danger btn-round btn-just-icon').click(function () { deleteTeam(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("delete")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ));

    var old_team = $("#team_id" + event.data.id);
    if (old_team.length > 0) {
        old_team.replaceWith(team);
    } else {
        team_table.append(team);
    }
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

function onTeamIDChange(ip_pattern) {
    new_team_ip.val(ip_pattern.replace(/%[s|d]/, new_team_id.val()));
}

function addTeam() {
    var valid = true;

    if (new_team_name.val().length === 0) {
        new_team_name.parent().addClass('has-error').addClass('has-feedback');
        new_team_name.parent().append(
            $("<span></span>").attr('id', "new_team_name_feedback")
                .attr("class", "glyphicon glyphicon-warning-sign form-control-feedback")
        );

        valid = false;
    } else {
        new_team_name.parent().removeClass('has-error').removeClass('has-feedback');
        $("#new_team_name_feedback").remove();
    }

    if (new_team_id.val().length === 0 || parseInt(new_team_id.val()) < 0) {
        new_team_id.parent().addClass('has-error').addClass('has-feedback');
        new_team_id.parent().append(
            $("<span></span>").attr('id', "new_team_id_feedback")
                .attr("class", "glyphicon glyphicon-warning-sign form-control-feedback")
        );

        valid = false;
    } else {
        new_team_id.parent().removeClass('has-error').removeClass('has-feedback');
        $("#new_team_id_feedback").remove();
    }

    if (valid) {
        stompClient.send("/ws/team/add", {}, JSON.stringify({
            id: new_team_id.val(),
            name: new_team_name.val(),
            ip: new_team_ip.val()
        }));
        $("#addTeam").modal('hide');
    }
}


function toggleTeam(team) {
    stompClient.send("/ws/team/toggle", {}, JSON.stringify({
        id: team
    }));
}

function deleteTeam(team) {
    stompClient.send("/ws/team/delete", {}, JSON.stringify({
        id: team
    }));
}