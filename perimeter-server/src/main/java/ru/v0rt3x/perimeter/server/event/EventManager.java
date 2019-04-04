package ru.v0rt3x.perimeter.server.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.perimeter.server.event.dao.EventType;

import java.util.List;

@Component
public class EventManager {

    @Autowired
    private EventRepository eventRepository;

    public void createEvent(EventType level, String message, Object... args) {
        Event event = new Event();

        event.setCreated(System.currentTimeMillis());
        event.setMessage(String.format(message, args));
        event.setType(level);

        eventRepository.save(event);
    }

    public void createEvent(String message, Object... args) {
        createEvent(EventType.INFO, message, args);
    }

    public List<Event> getLastEvents(int number, long after) {
        return eventRepository.findAllByCreatedGreaterThanOrderByCreatedDesc(after, PageRequest.of(0, number));
    }
}
