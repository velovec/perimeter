eventHandlers = {
    update_service: onServiceUpdate,
    delete_service: onServiceDelete,
    sync_service: onServiceSync
};

var service_sync_table = $("#service_sync");
var service_sync_data = null;

var new_service_name = $("#new_service_name");
var new_service_port = $("#new_service_port");

var service_table = $("#services");

function onServiceUpdate(event) {
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

    var old_service = $("#service_id" + event.data.id);
    if (old_service.length > 0) {
        old_service.replaceWith(service);
    } else {
        service_table.append(service);
    }
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
    stompClient.send("/app/service/request_sync", {}, null);
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
            stompClient.send("/app/service/confirm_sync", {}, JSON.stringify(service_sync_data));
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

function addService() {
    var valid = true;

    if (new_service_port.val().length === 0 || parseInt(new_service_port.val()) < 1 || parseInt(new_service_port.val()) > 65535) {
        new_service_port.parent().addClass('has-error').addClass('has-feedback');
        new_service_port.parent().append(
            $("<span></span>").attr('id', "new_service_port_feedback")
                .attr("class", "glyphicon glyphicon-warning-sign form-control-feedback")
        );

        valid = false;
    } else {
        new_service_port.parent().removeClass('has-error').removeClass('has-feedback');
        $("#new_service_port_feedback").remove();
    }

    if (new_service_name.val().length === 0) {
        new_service_name.parent().addClass('has-error').addClass('has-feedback');
        new_service_name.parent().append(
            $("<span></span>").attr('id', "new_service_name_feedback")
                .attr("class", "glyphicon glyphicon-warning-sign form-control-feedback")
        );

        valid = false;
    } else {
        new_service_name.parent().removeClass('has-error').removeClass('has-feedback');
        $("#new_service_name_feedback").remove();
    }

    if (valid) {
        stompClient.send("/app/service/add", {}, JSON.stringify({
            name: new_service_name.val(),
            port: parseInt(new_service_port.val())
        }));
        $("#addService").modal('hide');
    }
}

function deleteService(service) {
    stompClient.send("/app/service/delete", {}, JSON.stringify({
        id: service
    }));
}