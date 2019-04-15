package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;

import java.io.IOException;
import java.util.Objects;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.YELLOW;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.NORMAL;

public class AgentInfoWindow extends Window {

    private final AgentRepository agentRepository;

    public AgentInfoWindow(CursesConsoleUtils curses, AgentRepository agentRepository) {
        super(curses, "Agent Info", 2, 73, 19, 34, YELLOW, BRIGHT_WHITE, null);

        this.agentRepository = agentRepository;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, String.format("Agent On-Line: %s", agentRepository.count()));

        write(4, 2, BRIGHT_WHITE, BOLD, "Hostname");
        write(4, 15, BRIGHT_WHITE, BOLD, "Type");
        write(4, 24, BRIGHT_WHITE, BOLD, "Task");

        int line = 0;
        for (Agent agent: agentRepository.findAll()) {
            write(line + 5, 2, BRIGHT_WHITE, NORMAL, curses.wrapLine(agent.getHostName(), 12));
            write(line + 5, 15, BRIGHT_WHITE, NORMAL, curses.wrapLine(agent.getType(), 8));
            write(line + 5, 24,  BRIGHT_WHITE, NORMAL, curses.wrapLine(Objects.nonNull(agent.getTask()) ? agent.getTask() : "noop", 8));

            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
