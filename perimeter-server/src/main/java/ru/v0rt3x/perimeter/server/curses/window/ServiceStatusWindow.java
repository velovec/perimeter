package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.service.dao.ServiceRepository;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.*;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.NORMAL;

public class ServiceStatusWindow extends Window {

    private final ServiceRepository serviceRepository;

    public ServiceStatusWindow(CursesConsoleUtils curses, ServiceRepository serviceRepository) {
        super(curses, "Service Status", 2, 108, 19, 31, GREEN, BRIGHT_WHITE, null);

        this.serviceRepository = serviceRepository;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, String.format("Services Registered: %s", serviceRepository.count()));

        write(4, 2, BRIGHT_WHITE, BOLD, "Name");
        write(4, 15, BRIGHT_WHITE, BOLD, "Port");
        write(4, 21, BRIGHT_WHITE, BOLD, "Status");

        int line = 5;
        for (Service service: serviceRepository.findAll()) {
            ConsoleColor statusColor;

            switch (service.getStatus()) {
                case "UP":
                    statusColor = BRIGHT_GREEN;
                    break;
                case "DOWN":
                    statusColor = BRIGHT_RED;
                    break;
                default:
                    statusColor = WHITE;
                    break;
            }

            write(line, 2, BRIGHT_WHITE, NORMAL, curses.wrapLine(service.getName(), 12));
            write(line, 15, BRIGHT_WHITE, NORMAL, String.format("%-5d", service.getPort()));
            write(line, 21, statusColor, NORMAL, curses.wrapLine(service.getStatusString(), 8));

            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
