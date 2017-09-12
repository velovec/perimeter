package ru.v0rt3x.perimeter.server.web.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.web.events.types.NotificationEvent;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventProducer {

    @Autowired
    private SimpMessageSendingOperations messageTemplate;

    public void sendNotificationEvent(String type, String message) {
        notify("notification", new NotificationEvent(type, message));
    }

    public <T> List<T> saveAndNotify(CrudRepository<T, Long> repository, List<T> list) {
        return list.stream()
                .map(object -> saveAndNotify(repository, object))
                .collect(Collectors.toList());
    }

    public <T> T saveAndNotify(CrudRepository<T, Long> repository, T object) {
        T result = repository.save(object);

        notify(String.format("update_%s", object.getClass().getSimpleName().toLowerCase()), result);

        return result;
    }

    public <T> void deleteAndNotify(CrudRepository<T, Long> repository, List<T> list) {
        list.forEach(object -> deleteAndNotify(repository, object));
    }

    public <T> void deleteAndNotify(CrudRepository<T, Long> repository, T object) {
        repository.delete(object);
        notify(String.format("delete_%s", object.getClass().getSimpleName().toLowerCase()), object);
    }

    public void notify(String eventType, Object eventData) {
        messageTemplate.convertAndSend("/topic/perimeter", new EventWrapper(eventType, eventData));
    }
}
