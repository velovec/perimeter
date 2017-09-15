eventHandlers = {
    update_tcppacket: onTCPPacketUpdate,
    delete_tcppacket: onTCPPacketDelete,
    view_tcppacket: onTCPPacketView
};

var packet_table = $("#packets");

var packet_view = $("#packetView");
var packet_view_hex = $("#packetHEX");
var packet_view_ascii = $("#packetASCII");

var view_id = 0;

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
    stompClient.send("/ws/packet/view", {}, JSON.stringify({
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