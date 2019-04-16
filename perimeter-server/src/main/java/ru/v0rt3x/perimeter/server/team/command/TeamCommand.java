package ru.v0rt3x.perimeter.server.team.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;
import ru.v0rt3x.perimeter.server.team.TeamManager;
import ru.v0rt3x.perimeter.server.team.dao.Team;

import java.io.IOException;
import java.util.Objects;

@ShellCommand(command = "team", description = "Manage teams")
public class TeamCommand extends PerimeterShellCommand {

    private TeamManager teamManager;

    @Override
    protected void init() throws IOException {
        teamManager = context.getBean(TeamManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Add team")
    public void add() throws IOException {
        if (args.size() < 2) {
            console.error("team add [--guest <true|false>] [--ip <IP>] <id> <name>");
            exit(1);
            return;
        }

        if (kwargs.containsKey("ip")) {
            teamManager.addTeam(
                Integer.parseInt(args.get(0)), args.get(1), kwargs.get("ip"),
                (kwargs.containsKey("guest")) && Boolean.parseBoolean(kwargs.get("guest"))
            );
        } else {
            teamManager.addTeam(
                Integer.parseInt(args.get(0)), args.get(1),
                (kwargs.containsKey("guest")) && Boolean.parseBoolean(kwargs.get("guest"))
            );
        }

        console.writeLine("Team created");
    }

    @CommandAction("Enable attacks to team")
    public void enable() throws IOException {
        if (args.size() < 1) {
            console.error("team enable <team>");
            exit(1);
            return;
        }

        Team team = teamManager.getTeam(args.get(0));
        if (Objects.isNull(team)) {
            console.error("Team '%s' not found", args.get(0));
            exit(1);
            return;
        }

        if (team.isActive()) {
            console.error("Already enabled");
            exit(1);
            return;
        }

        teamManager.setActive(team, true);
        console.writeLine("Enabled");
    }


    @CommandAction("Disable attacks to team")
    public void disable() throws IOException {
        if (args.size() < 1) {
            console.error("team disable <team>");
            exit(1);
            return;
        }

        Team team = teamManager.getTeam(args.get(0));
        if (Objects.isNull(team)) {
            console.error("Team '%s' not found", args.get(0));
            exit(1);
            return;
        }

        if (!team.isActive()) {
            console.error("Already disabled");
            exit(1);
            return;
        }

        teamManager.setActive(team, false);
        console.writeLine("Disabled");
    }

    @CommandAction("List teams")
    public void list() throws IOException {
        Table teamList = new Table("Name", "IP", "Guest", "Active");

        for (Team team: teamManager.listTeams()) {
            teamList.addRow(team.getName(), team.getIp(), team.isGuest(), team.isActive());
        }

        console.write(teamList);
    }

    @Override
    protected void onInterrupt() {

    }
}
