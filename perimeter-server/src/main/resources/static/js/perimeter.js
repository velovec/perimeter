var stompClient = null;
var eventHandlers = {};

var status_element = $("#connection_status");
var status_icon = $("#connection_status_icon");

var contest_status_element = $("#connection_status");
var contest_status_icon = $("#connection_status");

function connect() {
    var options = { debug : false };

    setStatus("warning", "autorenew");

    var socket = new SockJS('/perimeter', undefined, options);
    stompClient = Stomp.over(socket);
    stompClient.debug = function (msg) { };
    stompClient.connect({}, onConnect, onError);
}

function onConnect(frame) {
    setStatus("success", "done_all");

    stompClient.subscribe("/topic/perimeter", function (event)  { onEvent(JSON.parse(event.body)) });
    stompClient.subscribe("/user/topic/perimeter", function (event)  { onEvent(JSON.parse(event.body)) });

    if ("on_connect" in eventHandlers) {
        eventHandlers["on_connect"]();
    }
}

function onEvent(event) {
    if (event.type === 'notification') {
        notify(event.data.type, event.data.message);
    } else if (event.type in eventHandlers) {
        eventHandlers[event.type](event);
    }
}

function onError(error) {
    setStatus("danger", "error_outline");
    setTimeout(connect, 5000);
}

function setStatus(color, icon) {
    status_element.attr('class', status_element.attr('class').replace(/btn-(warning|danger|success)/, "btn-" + color));
    status_icon.text(icon);
}

function notify(type, message) {
    $.notify({
        message: message
    }, {
        type: type,
        newest_on_top: true
    });
}

$(function () {
    connect();

    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    $("#submit_flag").click(function () {
        var flag_element = $("#flag_to_submit");

        stompClient.send("/app/flag/send", {}, JSON.stringify({ "flag": flag_element.val() }));
        flag_element.val("");
    });
});

$(document).ready(function () {
    $('.modal').appendTo("body");
});