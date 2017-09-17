eventHandlers = {
    update_service: onServiceUpdate,
    delete_service: onServiceDelete,

    update_tcppacket: onTCPPacketUpdate,
    delete_tcppacket: onTCPPacketDelete,
    view_tcppacket: onTCPPacketView,
    search_tcppacket: onTCPPacketSearch
};

var packet_table = $("#packets");

var packet_view = $("#packetView");
var packet_view_hex = $("#packetHEX");
var packet_view_ascii = $("#packetASCII");

var filter_service = $("#filter_service");
var filter_direction = $("#filter_direction");
var filter_client = $("#filter_client");
var filter_transmission = $("#filter_transmission");

var view_id = 0;

function onServiceUpdate(event) {
    var service = $("<option></option>").attr("value", event.data.id).text(event.data.name);

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

function onTCPPacketUpdate(event) {
    var packet = $("<tr></tr>").attr("id", "packet_id" + event.data.id);

    packet.append($("<td></td>").text(event.data.service.name));
    packet.append($("<td></td>").append(
        $("<i></i>").attr("class", "material-icons text-" + (event.data.inbound ? "danger" : "success")).text("keyboard_arrow_" + (event.data.inbound ? "left" : "right"))
    ));
    packet.append($("<td></td>").text(event.data.clientHost + ":" + event.data.clientPort));
    packet.append($("<td></td>").text(atob(event.data.payload).length));
    packet.append($("<td></td>").text(event.data.transmission));
    packet.append($("<td></td>").text(event.data.time));
    packet.append($("<td></td>").append(
        $("<button></button>").attr('type', 'button').attr('class', 'btn btn-info btn-round btn-just-icon').click(function () { requestTCPPacketView(event.data.id); }).append(
            $("<i></i>").attr("class", "material-icons").text("search")
        ).append(
            $("<div></div>").attr("class", "ripple-container")
        )
    ));

    var old_packet = $("#packet_id" + event.data.id);
    if (old_packet.length > 0) {
        old_packet.replaceWith(packet);
    } else {
        packet_table.append(packet);
    }
}

function onTCPPacketDelete(event) {
    var packet_element = $("#packet_id" + event.data.id);

    if (packet_element.length >= 0) {
        packet_element.remove();
    }
}

function requestTCPPacketView(id) {
    view_id = id;
    stompClient.send("/app/packet/view", {}, JSON.stringify({
        id: id
    }));
}

function onTCPPacketView(event) {
    if (event.data.id === view_id) {
        packet_view_ascii.text(event.data.ascii);
        packet_view_hex.text(event.data.hex);
        packet_view.modal('show');
    }
}

function doTCPPacketSearch() {
    stompClient.send("/app/packet/search", {}, JSON.stringify({
        service: filter_service.val(),
        direction: filter_direction.val(),
        client: filter_client.val(),
        transmission: filter_transmission.val()
    }));
}

function onTCPPacketSearch(event) {
    packet_table.empty();

    event.data.forEach(function (packet, index, array) {
        onTCPPacketUpdate({ data: packet });
    });
}