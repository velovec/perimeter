package ru.v0rt3x.perimeter.server.agent.command;

import ru.v0rt3x.perimeter.server.agent.AgentManager;
import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "agent", description = "Manage remote agents")
public class AgentCommand extends PerimeterShellCommand {

    private AgentManager agentManager;

    @Override
    protected void init() throws IOException {
        agentManager = context.getBean(AgentManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List remote agents")
    public void list() throws IOException {
        Table agentList = new Table("UUID", "Host", "OS", "Type", "Task", "Last Seen");

        for (Agent agent: agentManager.listAgents()) {
            agentList.addRow(
                agent.getUuid(), agent.getHostName(),
                String.format("%s %s (%s)", agent.getOsName(), agent.getOsVersion(), agent.getOsArch()),
                agent.getType(), agent.getTask(),
                String.format("%.1f second(s) ago", (System.currentTimeMillis() - agent.getLastSeen()) / 1000.0)
            );
        }

        console.write(agentList);
    }

    @Override
    protected void onInterrupt() {

    }
}
