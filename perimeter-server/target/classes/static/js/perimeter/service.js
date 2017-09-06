eventHandlers = {
    update_service: onServiceUpdate,
    delete_service: onServiceDelete,
    sync_service: onServiceSync
};

var service_sync_table = $("#service_sync");
var service_sync_data = null;

var service_table = $("#services");

function onServiceUpdate(event) {
    onServiceDelete(event);

    var service = $("<tr></tr>").attr("id", "service_id" + event.data.id);

    service.append($("<td></td>").text(event.data.id));
    service.append($("<td></td>").text(event.data.name));
    service.append($("<td></td>").text(event.data.port));
    service.append($("<td></td>").append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-info btn-round btn-just-icon').click(function () { alert("Not implemented"); }).append(
            $("<i></i>").attr("class", "material-icons").text("search")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ).append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-' + (event.data.available ? 'success' : 'danger') + ' btn-round btn-just-icon').click(function () { alert("Not implemented"); }).append(
            $("<i></i>").attr("class", "material-icons").text(event.data.available ? "autorenew" : "error_outline")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ).append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-danger btn-round btn-just-icon').click(function () { deleteService(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("delete")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ));

    service_table.append(service);
}

function onServiceDelete(event) {
    var service_element = $("#service_id" + event.data.id);

    if (service_element.length >= 0) {
        service_element.remove();
    }
}

function onServiceSync(event) {
    var services = event.data;

    service_sync_table.empty();
    service_sync_data = services;

    for (var i = 0; i < services.length; i++) {
        var service = $("<tr></tr>").attr("id", "service_sync_id" + services[i].id);

        service.append($("<td></td>").text(services[i].id));
        service.append($("<td></td>").text(services[i].name));
        service.append($("<td></td>").append(
            $("<div></div>").attr("class", "form-group").append(
                $("<input>").attr("type", "text")
                    .attr("class", "form-control")
                    .attr("id", "service_sync_id" + services[i].id + "_port")
            )
        ));

        service_sync_table.append(service);
    }
}

function requestServiceSync() {
    stompClient.send("/ws/service/request_sync", {}, null);
}

function confirmServiceSync() {
    if (service_sync_data) {
        var valid = true;

        for (var i = 0; i < service_sync_data.length; i++) {
            var port_input = $("#service_sync_id" + service_sync_data[i].id + "_port");
            var port = port_input.val();

            if (port.length === 0 || parseInt(port) < 1 || parseInt(port) > 65535) {
                port_input.parent().addClass('has-error').addClass('has-feedback');
                port_input.parent().append(
                    $("<span></span>").attr('id', "service_sync_id" + service_sync_data[i].id + "_port_feedback")
                        .attr("class", "glyphicon glyphicon-warning-sign form-control-feedback")
                );

                valid = false;
            } else {
                port_input.parent().removeClass('has-error').removeClass('has-feedback');
                $("#service_sync_id" + service_sync_data[i].id + "_port_feedback").remove();
            }

            service_sync_data[i].port = port;
        }

        if (valid) {
            stompClient.send("/ws/service/confirm_sync", {}, JSON.stringify(service_sync_data));
            $("#syncServices").modal('hide');
            notify("success", "Service synchronization started");
        }
    } else {
        notify("warning", "No data to sync");
    }
}

function showTrafficForService(service) {
    alert("Not Implemented");
}

function toggleMonitoring(service) {
    alert("Not Implemented");
}

function deleteService(service) {
    alert("Not Implemented");
}