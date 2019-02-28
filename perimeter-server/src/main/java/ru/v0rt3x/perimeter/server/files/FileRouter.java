package ru.v0rt3x.perimeter.server.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileRouter {

    private final Map<String, FileRouteInfo> routes = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(FileRouter.class);

    public void addRoute(String name, Pattern route, FileRoute handler) {
        routes.put(name, new FileRouteInfo(route, handler));
    }

    public void deleteRoute(String name) {
        routes.remove(name);
    }

    public boolean routeFile(String user, String path, byte[] data) {
        for (String route: routes.keySet()) {
            if (routes.get(route).route(user, path, data)) {
                logger.debug("Routing file '{}' to '{}'", path, route);
                return true;
            }
        }

        return false;
    }

    private class FileRouteInfo {

        private final Pattern pattern;
        private final FileRoute route;

        FileRouteInfo(Pattern pattern, FileRoute route) {
            this.pattern = pattern;
            this.route = route;
        }

        boolean route(String user, String path, byte[] data) {
            Matcher matcher = pattern.matcher(path);

            if (!matcher.matches())
                return false;

            try {
                route.route(user, matcher, data);
            } catch (Exception e) {
                logger.error("Unable to route file '{}': ({}) {}", path, e.getClass().getSimpleName(), e.getMessage());
            }

            return true;
        }
    }

    @FunctionalInterface
    public interface FileRoute {

        void route(String user, Matcher pathMatcher, byte[] data);

    }
}
