eventHandlers = {
    update_agent: onAgentUpdate,
    delete_agent: onAgentDelete
};

var agent_table = $("#agents");

function onAgentUpdate(event) {
    var agent = $("<tr></tr>").attr("id", "agent_id" + event.data.id);

    agent.append($("<td></td>").text(event.data.id));
    agent.append($("<td></td>").text(event.data.uuid));
    agent.append($("<td></td>").text(event.data.hostName));
    agent.append($("<td></td>").text(event.data.osName));
    agent.append($("<td></td>").text(event.data.osArch));
    agent.append($("<td></td>").text(event.data.task));
    agent.append($("<td></td>").append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-' + (event.data.available ? 'success' : 'danger') + ' btn-round btn-just-icon').append(
            $("<i></i>").attr("class", "material-icons").text(event.data.available ? "autorenew" : "error_outline")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ).append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-danger btn-round btn-just-icon').click(function () { deleteAgent(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("delete")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ));

    var old_agent = $("#agent_id" + event.data.id);
    if (old_agent.length > 0) {
        old_agent.replaceWith(agent);
    } else {
        agent_table.append(agent);
    }
}

function onAgentDelete(event) {
    var agent_element = $("#agent_id" + event.data.id);

    if (agent_element.length >= 0) {
        agent_element.remove();
    }
}

function deleteAgent(agent) {
    stompClient.send("/ws/agent/delete", {}, JSON.stringify({
        id: agent
    }));
}