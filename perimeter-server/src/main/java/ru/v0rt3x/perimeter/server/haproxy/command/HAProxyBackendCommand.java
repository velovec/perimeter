package ru.v0rt3x.perimeter.server.haproxy.command;

import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyBackend;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "backend", description = "Manage HAProxy backends")
public class HAProxyBackendCommand extends PerimeterShellCommand {

    private HAProxyManager haProxyManager;

    @Override
    protected void init() throws IOException {
        haProxyManager = context.getBean(HAProxyManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List backends")
    public void list() throws IOException {
        Table aclList = new Table("Name", "Server");

        for (HAProxyBackend backend: haProxyManager.listBackends()) {
            for (String server: backend.getServers()) {
                aclList.addRow(backend.getName(), server);
            }
        }

        console.write(aclList);
    }

    @Override
    protected void onInterrupt() {

    }
}
