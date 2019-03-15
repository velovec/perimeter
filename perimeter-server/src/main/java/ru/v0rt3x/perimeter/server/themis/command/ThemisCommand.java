package ru.v0rt3x.perimeter.server.themis.command;

import ru.v0rt3x.perimeter.server.flag.FlagInfo;
import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;
import ru.v0rt3x.perimeter.server.team.TeamManager;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@ShellCommand(command = "themis", description = "Themis integration")
public class ThemisCommand extends PerimeterShellCommand {

    private ThemisClient themisClient;
    private PerimeterProperties.ThemisProperties themisProperties;

    @Override
    protected void init() throws IOException {
        themisClient = context.getBean(ThemisClient.class);
        themisProperties = context.getBean(PerimeterProperties.class).getThemis();
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Get contest status from Themis")
    public void status() throws IOException {
        if (!themisProperties.isIntegrationEnabled()) {
            console.writeLine("Themis integration disabled");
            return;
        }

        console.writeLine("We are: %s", themisClient.getIdentity().getName());

        console.writeLine("Contest status: %s", themisClient.getContestState());
        console.writeLine("Contest round: %s", themisClient.getContestRound());
    }

    @CommandAction("Get JWT public key from Themis")
    public void public_key() throws IOException {
        if (!themisProperties.isIntegrationEnabled()) {
            console.writeLine("Themis integration disabled");
            return;
        }

        String publicKey = themisClient.getPublicKey();

        context.getBean(FlagProcessor.class).setThemisPublicKey(publicKey);

        console.writeLine("Public Key:");
        console.writeLine(publicKey);
    }

    @CommandAction("Show flag info")
    public void flag_info() throws IOException {
        if (args.size() < 1) {
            console.writeLine("themis flag_info <flag>");
            exit(1);
            return;
        }

        if (!context.getBean(PerimeterProperties.class).getFlag().getPattern().matcher(args.get(0)).matches()) {
            console.writeLine("Invalid flag");
            exit(1);
            return;
        }

        FlagInfo flagInfo = themisClient.getFlagInfo(Flag.newFlag(args.get(0), FlagPriority.NORMAL));
        if (Objects.nonNull(flagInfo)) {
            if (flagInfo.isValid()) {
                console.write(flagInfo.toTable());
            } else {
                console.writeLine("Flag doesn't exists");
            }
        } else {
            console.error("Unable to get flag info");
        }
    }

    @CommandAction("Submit flag")
    public void submit() throws IOException {
        if (args.size() < 1) {
            console.writeLine("themis submit <flag>");
            exit(1);
            return;
        }

        if (!context.getBean(PerimeterProperties.class).getFlag().getPattern().matcher(args.get(0)).matches()) {
            console.writeLine("Invalid flag");
            exit(1);
            return;
        }

        FlagResult result = themisClient.submitFlag(Flag.newFlag(args.get(0), FlagPriority.NORMAL));
        console.writeLine(result.toString());
    }

    @CommandAction("Sync team list from Themis")
    public void sync() throws IOException {
        if (!themisProperties.isIntegrationEnabled()) {
            console.writeLine("Themis integration disabled");
            return;
        }

        Table teamListTable = new Table("ID", "Name", "IP", "Is Guest");

        List<Team> teamList = themisClient.getTeamList();
        for (Team team: teamList) {
            teamListTable.addRow(team.getId(), team.getName(), team.getIp(), team.isGuest());
        }
        console.write(teamListTable);

        if (console.readYesNo("Update database?")) {
            context.getBean(TeamManager.class).replaceTeams(teamList);
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
