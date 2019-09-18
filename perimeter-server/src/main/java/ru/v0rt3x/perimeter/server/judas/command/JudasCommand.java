package ru.v0rt3x.perimeter.server.judas.command;

import ru.v0rt3x.perimeter.server.judas.JudasManager;
import ru.v0rt3x.perimeter.server.judas.dao.JudasTarget;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "judas", description = "Manage Judas targets")
public class JudasCommand extends PerimeterShellCommand {

    private JudasManager judasManager;

    @Override
    protected void init() throws IOException {
        judasManager = context.getBean(JudasManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List Judas targets")
    public void list() throws IOException {
        Table targetList = new Table("ID", "Description", "Host", "Port");

        for (JudasTarget target: judasManager.listTargets()) {
            targetList.addRow(
                target.getId(), target.getDescription(), target.getHost(), target.getPort()
            );
        }

        console.write(targetList);
    }

    @CommandAction("Set Judas target")
    public void set() throws IOException {
        if (args.size() < 1) {
            console.error("judas set <port> [<host>]");
            exit(1);
            return;
        }

        JudasTarget target = judasManager.setTarget(Integer.parseInt(args.get(0)), args.size() > 1 ? args.get(1) : null);

        console.writeLine(
            "Judas target for '%s' (port: %d) set to: %s",
            target.getDescription(), target.getPort(), target.getHost()
        );
    }

    @Override
    protected void onInterrupt() {

    }
}
