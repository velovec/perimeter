package ru.v0rt3x.perimeter.server.web.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.web.events.types.NotificationEvent;

@Component
public class EventEmitter {

    @Autowired
    private SimpMessageSendingOperations messageTemplate;

    public void sendEvent(String eventType, Object eventData) {
        messageTemplate.convertAndSend("/topic/perimeter", new EventWrapper(eventType, eventData));
    }

    public void sendNotificationEvent(String type, String message) {
        sendEvent("notification", new NotificationEvent(type, message));
    }
}
