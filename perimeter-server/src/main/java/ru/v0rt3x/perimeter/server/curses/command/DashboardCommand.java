package ru.v0rt3x.perimeter.server.curses.command;


import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.CursesInputHandler;
import ru.v0rt3x.perimeter.server.curses.CursesMouseInputHandler;
import ru.v0rt3x.perimeter.server.curses.utils.*;
import ru.v0rt3x.perimeter.server.curses.window.*;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResultRepository;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitRepository;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.service.dao.ServiceRepository;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;

import java.io.IOException;
import java.util.*;

@ShellCommand(command = "dashboard", description = "(EXPERIMENTAL) Run Curses dashboard")
public class DashboardCommand extends PerimeterShellCommand {

    private CursesConsoleUtils curses;
    private Timer timer;

    private final List<Window> windows = new ArrayList<>();

    @Override
    protected void init() throws IOException {
        curses = new CursesConsoleUtils(
            input, output, error, getEnvironment()
        );

        curses.mouseEnable();

        curses.onKeyPress((keyCode) -> {
            for (Window window: windows) {
                window.onKeyPressEvent(keyCode);
            }
        });
        curses.onMouseClick((mouseKeyCode) -> {
            for (Window window: windows) {
                window.onMouseClickEvent(mouseKeyCode);
            }
        });

        curses.onKeyPress((keyCode) -> {
            curses.clear();
            curses.mouseDisable();
            destroy();
        }, KeyCode.controlOf('C'), KeyCode.controlOf('D'));

        curses.onKeyPress((keyCode) -> redraw(), KeyCode.of('r'));

        curses.onScreenSizeChange(this::redraw);

        timer = new Timer();

        windows.add(new PerimeterMainWindow(curses));
        windows.add(new FlagHistoryWindow(curses, context.getBean(FlagRepository.class)));
        windows.add(new ThemisInfoWindow(curses, context.getBean(ThemisClient.class)));
        windows.add(new FlagQueueStatsWindow(curses, context.getBean(FlagRepository.class)));
        windows.add(new ExploitWindow(curses, context.getBean(ExploitRepository.class), context.getBean(ExploitExecutionResultRepository.class)));
        windows.add(new AgentInfoWindow(curses, context.getBean(AgentRepository.class)));
        windows.add(new ServiceStatusWindow(curses, context.getBean(ServiceRepository.class)));
        windows.add(new TeamStatisticWindow(curses, context.getBean(ExploitExecutionResultRepository.class)));
        windows.add(new EventLogWindow(curses, context.getBean(EventRepository.class)));
    }

    @Override
    protected void execute() throws IOException {
        // showLogo();

        curses.init();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    redraw();
                } catch (IOException e) {
                    // TODO: Add error dialog
                }
            }
        }, 0L, 5000L);

        while (isRunning()) {
            curses.read();
            sleep(100L);
        }
    }

//    private void showLogo() throws IOException {
//        curses.init();
//
//        Rectangle logo = Rectangle.newRect(curses.getScreen(), 0.5f, 0.5f, 40, 102);
//        if (!curses.isPossibleToRender(logo))
//            return;
//
//        curses.draw(logo, BLACK, BRIGHT_WHITE, "Powered By");
//        int lineNum = 1;
//        for (String line: v0rt3xLogo.LOGO.split("\n")) {
//            curses.write(logo, lineNum, 1, null, BRIGHT_WHITE, BOLD, line);
//            lineNum++;
//        }
//        sleep(2000L);
//    }

    private void redraw() throws IOException {
        for (Window window: windows) {
            window.draw();
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
