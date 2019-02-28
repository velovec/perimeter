package ru.v0rt3x.perimeter.server.haproxy.command;

import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyACL;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "acl", description = "Manage HAProxy ACLs")
public class HAProxyACLCommand extends PerimeterShellCommand {

    private HAProxyManager haProxyManager;

    @Override
    protected void init() throws IOException {
        haProxyManager = context.getBean(HAProxyManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List ACLs")
    public void list() throws IOException {
        Table aclList = new Table("Name", "Rule");

        for (HAProxyACL acl: haProxyManager.listACLs()) {
            for (String rule: acl.getRules()) {
                aclList.addRow(acl.getName(), rule);
            }
        }

        console.write(aclList);
    }

    @Override
    protected void onInterrupt() {

    }
}
