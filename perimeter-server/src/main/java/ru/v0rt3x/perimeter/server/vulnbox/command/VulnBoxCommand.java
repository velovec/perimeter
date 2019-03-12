package ru.v0rt3x.perimeter.server.vulnbox.command;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.utils.SSHUtils;
import ru.v0rt3x.perimeter.server.vulnbox.VulnBoxUserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ShellCommand(command = "vulnbox", description = "VulnBox manager")
public class VulnBoxCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {

    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Scan VulnBox LXC containers")
    public void lxc() throws IOException {
        try {
            Session session = getSSHSession();

            for (String line: executeCommand(session, "lxc ls")) {
                console.writeLine(line);
            }

            for (String line: executeCommand(session, "sudo iptables -t nat -L | grep DNAT")) {
                console.writeLine(line);
            }

            session.disconnect();
        } catch (JSchException e) {
            console.write(e);
        }
    }

    @CommandAction("Execute command on VulnBox")
    public void exec() throws IOException {
        if (args.size() < 1) {
            console.writeLine("lxc [--user <user>] [--port <port>] [--host <host>] [--password <password>] <command>");
            exit(1);
            return;
        }

        try {
            Session session = getSSHSession();

            for (String line: executeCommand(session, String.join(" ", args))) {
                console.writeLine(line);
            }

            session.disconnect();
        } catch (JSchException e) {
            console.write(e);
        }
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

    private List<String> executeCommand(Session session, String command) throws JSchException, IOException {
        Channel execChannel = session.openChannel("exec");

        execChannel.setInputStream(null);
        ((ChannelExec) execChannel).setErrStream(error);

        ((ChannelExec) execChannel).setCommand(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(execChannel.getInputStream()));
        execChannel.connect();

        List<String> output = new ArrayList<>();

        String line = null;
        while (!execChannel.isClosed() || Objects.nonNull(line)) {
            line = reader.readLine();
            output.add(line);

            sleep(100L);
        }

        execChannel.disconnect();

        return output;
    }
}
