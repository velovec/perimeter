package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.perimeter.server.dashboard.window.modal.ServiceContextMenuWindow;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.service.dao.ServiceRepository;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class ServiceStatusWindow extends Window {

    private final ServiceRepository serviceRepository;

    public ServiceStatusWindow(WindowManager windowManager) {
        super(windowManager,"Service Status", 2, 108, 14, 34, GREEN, BRIGHT_WHITE, null, 1);

        this.serviceRepository = context.getBean(ServiceRepository.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, String.format("Services Registered: %s", serviceRepository.count()));

        write(4, 2, BRIGHT_WHITE, BOLD, "Name");
        write(4, 18, BRIGHT_WHITE, BOLD, "Port");
        write(4, 24, BRIGHT_WHITE, BOLD, "Status");

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

            Rectangle rect = contextMenu(line, 2, window.getWidth() - 4, () -> {
                ServiceContextMenuWindow contextMenuWindow = windowManager.createWindow(ServiceContextMenuWindow.class, "service_menu");
                contextMenuWindow.setService(service);

                contextMenuWindow.draw(true);
            });

            write(rect.getX(), 2, BRIGHT_WHITE, NORMAL, curses.wrapLine(service.getName(), 15));
            write(rect.getX(), 18, BRIGHT_WHITE, NORMAL, String.format("%-5d", service.getPort()));
            write(rect.getX(), 24, statusColor, NORMAL, curses.wrapLine(service.getStatusString(), 8));

            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
