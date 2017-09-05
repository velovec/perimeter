package ru.v0rt3x.perimeter.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.v0rt3x.perimeter.server.web.events.EventEmitter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

public abstract class UIBaseView {

    private static final Map<String, UIView> views = new HashMap<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected EventEmitter emitter;

    @PostConstruct
    protected void registerView() {
        if (!this.getClass().isAnnotationPresent(UIView.class))
            throw new IllegalStateException("UIBaseView implementations should has UIView annotation");

        UIView view = this.getClass().getAnnotation(UIView.class);

        views.put(view.name(), view);
    }

    @ModelAttribute("PAGE_NAV_LINKS")
    private List<UIView> navLinks() {
        List<UIView> menuLinks = new ArrayList<>(views.values());

        menuLinks.sort(Comparator.comparingInt(UIView::linkOrder));

        return menuLinks;
    }

    @ModelAttribute("PAGE_TITLE")
    private String title() {
        if (getClass().isAnnotationPresent(UIView.class)) {
            return getClass().getAnnotation(UIView.class).title();
        }

        return "Unknown Page";
    }

    @ModelAttribute("PAGE_SCRIPT")
    private String script() {
        if (getClass().isAnnotationPresent(UIView.class)) {
            return getClass().getAnnotation(UIView.class).name() + ".js";
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected void addNavButton(Map<String, Object> context, String icon, String color, String options) {
        if (!context.containsKey("PAGE_NAV_BUTTONS")) {
            context.put("PAGE_NAV_BUTTONS", new ArrayList<UIButton>());
        }

        ((List<UIButton>) context.get("PAGE_NAV_BUTTONS")).add(
            new UIButton(icon, color, options)
        );
    }

    protected <T> List<T> saveAndNotify(CrudRepository<T, Long> repository, List<T> list) {
        return list.stream()
            .map(object -> saveAndNotify(repository, object))
            .collect(Collectors.toList());
    }

    protected <T> T saveAndNotify(CrudRepository<T, Long> repository, T object) {
        T result = repository.save(object);

        emitter.sendEvent(String.format("update_%s", object.getClass().getSimpleName().toLowerCase()), result);

        return result;
    }

    protected <T> void deleteAndNotify(CrudRepository<T, Long> repository, List<T> list) {
        list.forEach(object -> deleteAndNotify(repository, object));
    }

    protected <T> void deleteAndNotify(CrudRepository<T, Long> repository, T object) {
        repository.delete(object);
        emitter.sendEvent(String.format("delete_%s", object.getClass().getSimpleName().toLowerCase()), object);
    }

    protected void notify(String eventType, Object eventData) {
        emitter.sendEvent(eventType, eventData);
    }
}
