package ru.v0rt3x.perimeter.server.curses.command;

import org.springframework.data.domain.PageRequest;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.CursesInputHandler;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.Rectangle;
import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.perimeter.server.exploit.dao.Exploit;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResultRepository;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitRepository;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.service.dao.ServiceRepository;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.v0rt3xLogo;

import java.io.IOException;
import java.util.*;

import static ru.v0rt3x.perimeter.server.flag.dao.FlagStatus.*;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.*;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.*;

@ShellCommand(command = "dashboard", description = "(EXPERIMENTAL) Run Curses dashboard")
public class DashboardCommand extends PerimeterShellCommand implements CursesInputHandler {

    private CursesConsoleUtils curses;
    private Timer timer;

    private FlagRepository flagRepository;
    private AgentRepository agentRepository;
    private ExploitRepository exploitRepository;
    private ExploitExecutionResultRepository resultRepository;
    private ServiceRepository serviceRepository;
    private EventRepository eventRepository;

    private ThemisClient themisClient;

    private final Object lock = new Object();

    private boolean freeze = false;

    private int lastLog = 0;
    private int lastFlags = 0;
    private int lastAgents = 0;
    private int lastExploits = 0;
    private int lastServices = 0;
    private int lastTeams = 0;

    @Override
    protected void init() throws IOException {
        curses = new CursesConsoleUtils(
            input, output, error, getEnvironment(), lock
        );

        curses.onKeyPress(this);
        curses.onKeyPress((keyCode) -> {
            curses.clear();
            destroy();
        }, KeyCode.ESCAPE, KeyCode.controlOf('C'), KeyCode.controlOf('D'));
        curses.onKeyPress((keyCode) -> redraw(), KeyCode.of('r'));
        curses.onKeyPress((keyCode) -> freeze(), KeyCode.of('f'));

        curses.onScreenSizeChange(this::redraw);

        timer = new Timer();

        flagRepository = context.getBean(FlagRepository.class);
        agentRepository = context.getBean(AgentRepository.class);
        exploitRepository = context.getBean(ExploitRepository.class);
        resultRepository = context.getBean(ExploitExecutionResultRepository.class);
        serviceRepository = context.getBean(ServiceRepository.class);
        eventRepository = context.getBean(EventRepository.class);

        themisClient = context.getBean(ThemisClient.class);
    }

    @Override
    protected void execute() throws IOException {
        showLogo();

        curses.init(CYAN, BRIGHT_WHITE, "Perimeter Shell");
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

    private void showLogo() throws IOException {
        curses.init(CYAN, BRIGHT_WHITE, "Perimeter Shell");

        Rectangle logo = Rectangle.newRect(curses.getScreen(), 0.5f, 0.5f, 40, 102);
        if (!curses.isPossibleToRender(logo))
            return;

        curses.draw(logo, BLACK, BRIGHT_WHITE, "Powered By");
        int lineNum = 1;
        for (String line: v0rt3xLogo.LOGO.split("\n")) {
            curses.write(logo, lineNum, 1, null, BRIGHT_WHITE, BOLD, line);
            lineNum++;
        }
        sleep(2000L);
    }

    private void redraw() throws IOException {
        if (freeze) {
            return;
        }

        synchronized (lock) {
            drawThemisInfo();
            drawLastFlags();
            drawFlagStats();
            drawAgentInfo();
            drawExploitStats();
            drawServiceStatus();
            drawTeamStats();
            drawEventLog();
            drawSize();
        }
    }

    private void freeze() throws IOException {
        this.freeze = !this.freeze;

        if (this.freeze) {
            curses.write(0, curses.getScreenWidth() - 7, CYAN, BRIGHT_WHITE, BOLD, "FREEZE");
        } else {
            curses.draw(Rectangle.newLine(0, curses.getScreenWidth() - 7, 6), CYAN);
        }
    }

    private void drawThemisInfo() throws IOException {
        Rectangle themisInfo = Rectangle.newRect(2, 49, 6, 23);
        if (!curses.isPossibleToRender(themisInfo))
            return;

        curses.draw(themisInfo, BLUE, BRIGHT_WHITE, "Themis Info");

        curses.write(themisInfo, 2, 2, null, BRIGHT_WHITE, BOLD, String.format("State: %s", themisClient.getContestState()));
        curses.write(themisInfo, 3, 2, null, BRIGHT_WHITE, BOLD, String.format("Round: %d", themisClient.getContestRound()));
    }

    private void drawFlagStats() throws IOException {
        Rectangle flagStats = Rectangle.newRect(9, 49, 12, 23);
        if (!curses.isPossibleToRender(flagStats))
            return;

        curses.draw(flagStats, MAGENTA, BRIGHT_WHITE, "Flag Queue");

        curses.write(flagStats, 2, 2, null, BRIGHT_WHITE, BOLD, "Queued");
        curses.write(flagStats, 4, 2, null, BRIGHT_WHITE, BOLD, "\u2523 Low");
        curses.write(flagStats, 5, 2, null, BRIGHT_WHITE, BOLD, "\u2523 Normal");
        curses.write(flagStats, 6, 2, null, BRIGHT_WHITE, BOLD, "\u2517 High");

        curses.write(flagStats, 8, 2, null, BRIGHT_WHITE, BOLD, "Accepted");
        curses.write(flagStats, 9, 2, null, BRIGHT_WHITE, BOLD, "Rejected");

        int offset = flagStats.getWidth() - 11;

        curses.write(flagStats, 4, offset, null, BRIGHT_WHITE, NORMAL, String.format(" %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.LOW)));
        curses.write(flagStats, 5, offset, null, BRIGHT_WHITE, NORMAL, String.format(" %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.NORMAL)));
        curses.write(flagStats, 6, offset, null, BRIGHT_WHITE, NORMAL, String.format(" %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.HIGH)));

        curses.write(flagStats, 8, offset, null, BRIGHT_WHITE, NORMAL, String.format(" %08d", flagRepository.countAllByStatus(ACCEPTED)));
        curses.write(flagStats, 9, offset, null, BRIGHT_WHITE, NORMAL, String.format(" %08d", flagRepository.countAllByStatus(REJECTED)));
    }

    private void drawLastFlags() throws IOException {
        Rectangle flagHistory = Rectangle.newRect(2, 2, Math.max(3, curses.getScreenHeight() - 4), 46);
        if (!curses.isPossibleToRender(flagHistory))
            return;

        curses.draw(flagHistory, GREEN, BRIGHT_WHITE, "Flag History");

        curses.erase(flagHistory, 1, lastFlags, null);

        int line = 0;
        for (Flag flag: flagRepository.findAllByOrderByCreateTimeStampDesc(PageRequest.of(0, flagHistory.getHeight() - 2))) {
            ConsoleColor flagColor;

            switch (flag.getStatus()) {
                case QUEUED:
                    flagColor = BRIGHT_WHITE;
                    break;
                case ACCEPTED:
                    flagColor = BRIGHT_GREEN;
                    break;
                case REJECTED:
                    flagColor = BRIGHT_RED;
                    break;
                default:
                    flagColor = WHITE;
            }

            curses.write(flagHistory, line + 1, 2, null, WHITE, NORMAL, flag.getFlag());
            curses.write(flagHistory, line + 1, 36, null, flagColor, NORMAL, curses.wrapLine(flag.getStatus().name(), 9));
            line++;
        }
        lastFlags = line;
    }

    private void drawAgentInfo() throws IOException {
        Rectangle agentInfo = Rectangle.newRect(2, 73, 19, 34);
        if (!curses.isPossibleToRender(agentInfo))
            return;

        curses.draw(agentInfo, YELLOW, BRIGHT_WHITE, "Agent Info");

        curses.write(agentInfo, 2, 2, null, BRIGHT_WHITE, BOLD, String.format("Agent On-Line: %s", agentRepository.count()));

        curses.write(agentInfo, 4, 2, null, BRIGHT_WHITE, BOLD, "Hostname");
        curses.write(agentInfo, 4, 15, null, BRIGHT_WHITE, BOLD, "Type");
        curses.write(agentInfo, 4, 24, null, BRIGHT_WHITE, BOLD, "Task");

        curses.erase(agentInfo, 5, lastAgents, null);

        int line = 0;
        for (Agent agent: agentRepository.findAll()) {
            curses.write(agentInfo, line + 5, 2, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(agent.getHostName(), 12));
            curses.write(agentInfo, line + 5, 15, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(agent.getType(), 8));
            curses.write(agentInfo, line + 5, 24, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(Objects.nonNull(agent.getTask()) ? agent.getTask() : "noop", 8));

            line++;
        }
        lastAgents = line;
    }

    private void drawExploitStats() throws IOException {
        Rectangle exploitStats = Rectangle.newRect(22, 49, Math.max(7, curses.getScreenHeight() - 24), 58);
        if (!curses.isPossibleToRender(exploitStats))
            return;

        curses.draw(exploitStats, RED, BRIGHT_WHITE, "Exploit Statistics");

        curses.write(exploitStats, 2, 2, null, BRIGHT_WHITE, BOLD, String.format("Exploits Registered: %s", exploitRepository.count()));

        curses.write(exploitStats, 4, 2, null, BRIGHT_WHITE, BOLD, "Name");
        curses.write(exploitStats, 4, 15, null, BRIGHT_WHITE, BOLD, "Type");
        curses.write(exploitStats, 4, 24, null, BRIGHT_WHITE, BOLD, "Priority");
        curses.write(exploitStats, 4, 33, null, BRIGHT_WHITE, BOLD, "Hits");
        curses.write(exploitStats, 4, 41, null, BRIGHT_WHITE, BOLD, "Last Run");

        curses.erase(exploitStats, 5, lastExploits, null);

        int line = 0;
        for (Exploit exploit: exploitRepository.findAll()) {
            long countTotal = resultRepository.countAllByExploit(exploit);
            long countSuccess = resultRepository.findAllByExploit(exploit).stream()
                .filter(result -> result.getExitCode() == 0)
                .count();

            ConsoleColor statusColor = (countSuccess == countTotal) ? BRIGHT_GREEN : BRIGHT_RED;

            curses.write(exploitStats, line + 5, 2, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(exploit.getName(), 12));
            curses.write(exploitStats, line + 5, 15, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(exploit.getType(), 8));
            curses.write(exploitStats, line + 5, 24, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(exploit.getPriority().toString(), 8));
            curses.write(exploitStats, line + 5, 33, null, BRIGHT_WHITE, NORMAL, String.format("%07d", exploit.getHits()));
            curses.write(exploitStats, line + 5, 41, null, statusColor, NORMAL, String.format("%02d / %02d", countSuccess, countTotal));

            line++;
        }
        lastExploits = line;
    }

    private void drawServiceStatus() throws IOException {
        Rectangle serviceStatus = Rectangle.newRect(2, 108, 19, 31);
        if (!curses.isPossibleToRender(serviceStatus))
            return;

        curses.draw(serviceStatus, GREEN, BRIGHT_WHITE, "Service Status");

        curses.write(serviceStatus, 2, 2, null, BRIGHT_WHITE, BOLD, String.format("Services Registered: %s", serviceRepository.count()));

        curses.write(serviceStatus, 4, 2, null, BRIGHT_WHITE, BOLD, "Name");
        curses.write(serviceStatus, 4, 15, null, BRIGHT_WHITE, BOLD, "Port");
        curses.write(serviceStatus, 4, 21, null, BRIGHT_WHITE, BOLD, "Status");

        curses.erase(serviceStatus, 5, lastServices, null);

        int line = 0;
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

            curses.write(serviceStatus, line + 5, 2, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(service.getName(), 12));
            curses.write(serviceStatus, line + 5, 15, null, BRIGHT_WHITE, NORMAL, String.format("%-5d", service.getPort()));
            curses.write(serviceStatus, line + 5, 21, null, statusColor, NORMAL, curses.wrapLine(service.getStatusString(), 8));

            line++;
        }
        lastServices = line;
    }

    private void drawTeamStats() throws IOException {
        Rectangle teamStats = Rectangle.newRect(22, 108, Math.max(7, curses.getScreenHeight() - 24), 31);
        if (!curses.isPossibleToRender(teamStats))
            return;

        curses.draw(teamStats, BLUE, BRIGHT_WHITE, "Team Statistics");

        curses.write(teamStats, 2, 2, null, BRIGHT_WHITE, BOLD, "Team");
        curses.write(teamStats, 2, 22, null, BRIGHT_WHITE, BOLD, "Hits");

        Map<String, Integer> teamHits = new HashMap<>();
        resultRepository.findAll().forEach(
            result -> {
                int hits = teamHits.getOrDefault(result.getTeam(), 0);

                teamHits.put(result.getTeam(), hits + result.getHits());
            }
        );

        curses.erase(teamStats, 4, lastTeams, null);

        int line = 0;
        for (String team: teamHits.keySet()) {
            curses.write(teamStats, line + 4, 2, null, BRIGHT_WHITE, NORMAL, curses.wrapLine(team, 19));
            curses.write(teamStats, line + 4, 22, null, BRIGHT_WHITE, NORMAL, String.format("%07d", teamHits.get(team)));
            line++;
        }
        lastTeams = line;
    }

    private void drawEventLog() throws IOException {
        Rectangle eventLog = Rectangle.newRect(2, 140, Math.max(7, curses.getScreenHeight() - 4), Math.max(50, curses.getScreenWidth() - 142));
        if (!curses.isPossibleToRender(eventLog))
            return;

        curses.draw(eventLog, MAGENTA, BRIGHT_WHITE, "Event Log");

        int size = eventLog.getHeight() - 2;

        curses.erase(eventLog, 1, lastLog, null);

        int line = 0;
        for (Event event: eventRepository.findAllByCreatedGreaterThanOrderByCreatedDesc(System.currentTimeMillis() - 600000L, PageRequest.of(0, size))) {
            long ago = (System.currentTimeMillis() - event.getCreated()) / 1000;

            ConsoleColor eventColor;
            switch (event.getType()) {
                case INFO:
                    eventColor = BRIGHT_WHITE;
                    break;
                case WARNING:
                    eventColor = BRIGHT_YELLOW;
                    break;
                case URGENT:
                    eventColor = BRIGHT_RED;
                    break;
                default:
                    eventColor = WHITE;
                    break;
            }

            curses.write(eventLog, line + 1, 2, null, eventColor, NORMAL, curses.wrapLine(event.getMessage(), eventLog.getWidth() - 13));
            curses.write(eventLog, line + 1, eventLog.getWidth() - 10, null, WHITE, NORMAL, String.format("%03ds ago", ago));
            line++;
        }
        lastLog = line;
    }

    private void drawSize() throws IOException {
        curses.write(
            curses.getScreenHeight() - 1, curses.getScreenWidth() - 8,
            CYAN, BRIGHT_WHITE, BOLD, String.format("%3dx%-3d", curses.getScreenHeight(), curses.getScreenWidth())
        );
    }

    @Override
    protected void onInterrupt() {

    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {
        // do nothing
    }
}
