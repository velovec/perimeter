package ru.v0rt3x.perimeter.server.dashboard.command;


import ru.v0rt3x.shell.curses.CursesEngine;
import ru.v0rt3x.perimeter.server.dashboard.window.*;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;
import java.util.*;

@ShellCommand(command = "dashboard", description = "(EXPERIMENTAL) Run Curses dashboard")
public class DashboardCommand extends PerimeterShellCommand {

    private CursesEngine curses;
    private Timer timer;

    private WindowManager windowManager;

    @Override
    protected void init() throws IOException {
        curses = new CursesEngine(
            input, output, error, getEnvironment()
        );

        windowManager = new WindowManager(curses, context, (wm) -> {
            wm.createWindow(PerimeterMainWindow.class, "main");
            wm.createWindow(FlagHistoryWindow.class, "flag_history");
            wm.createWindow(ThemisInfoWindow.class, "themis_info");
            wm.createWindow(FlagQueueStatsWindow.class, "flag_queue");
            wm.createWindow(ExploitWindow.class, "exploits");
            wm.createWindow(AgentInfoWindow.class, "agents");
            wm.createWindow(ServiceStatusWindow.class, "services");
            wm.createWindow(TeamStatisticWindow.class, "team_statistics");
            wm.createWindow(EventLogWindow.class, "event_log");
        }, !kwargs.containsKey("nosplash") ? SplashWindow.class : null, 2000L);

        curses.mouseEnable();

        curses.onKeyPress((keyCode) -> {
            curses.clear();
            curses.mouseDisable();
            destroy();
        }, KeyCode.controlOf('C'), KeyCode.controlOf('D'));

        timer = new Timer();

    }

    @Override
    protected void execute() throws IOException {
        windowManager.init();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    windowManager.draw();
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

    @Override
    protected void onInterrupt() {

    }
}
