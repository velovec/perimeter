package ru.v0rt3x.perimeter.server.vulnbox.command;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;
import ru.v0rt3x.perimeter.server.utils.SSHUtils;
import ru.v0rt3x.perimeter.server.vulnbox.VulnBoxUserInfo;
import ru.v0rt3x.perimeter.server.utils.iptables.IPTablesRule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ShellCommand(command = "vulnbox", description = "VulnBox manager")
public class VulnBoxCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {

    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Detect VulnBox services")
    public void detect_services() throws IOException {
        if (kwargs.containsKey("help")) {
            console.writeLine("vulnbox detect_services [--user <user>] [--port <port>] [--host <host>] [--password <password>]");
            return;
        }

        try {
            Session session = getSSHSession();
            Table services = new Table("Name", "Type", "Source", "Destination", "Chain", "Rule");

            console.writeLine("Detecting Docker services...");
            serviceListToTable(SSHUtils.detectDockerServices(session, error), "Docker", services);

            console.writeLine("Detecting LXC services...");
            serviceListToTable(SSHUtils.detectLXCServices(session, error), "LXC", services);

            console.newLine();

            console.write(services);
            session.disconnect();
        } catch (JSchException e) {
            console.write(e);
        }
    }

    private void serviceListToTable(Map<String, List<IPTablesRule>> services, String type, Table table) throws IOException {
        services.forEach((service, rules) -> {
            boolean isFirst = true;
            for (IPTablesRule rule : rules) {
                table.addRow(
                    isFirst ? service : "", isFirst ? type : "",
                    rule.getSource(), rule.getDestination(),
                    rule.getTarget(), rule.getExtra()
                );
                isFirst = false;
            }
        });
    }

    @Override
    protected void onInterrupt() {

    }

    private Session getSSHSession() throws JSchException {
        PerimeterProperties perimeterProperties = context.getBean(PerimeterProperties.class);

        return SSHUtils.getSession(
            kwargs.getOrDefault("user", getEnvironment().getEnv().get("USER")),
            kwargs.getOrDefault("host", perimeterProperties.getTeam().getInternalIp()),
            Integer.parseInt(kwargs.getOrDefault("port", "22")),
            kwargs.get("password"), new VulnBoxUserInfo(console)
        );
    }
}
