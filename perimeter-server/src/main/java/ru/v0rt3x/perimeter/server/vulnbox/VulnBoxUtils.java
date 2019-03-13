package ru.v0rt3x.perimeter.server.vulnbox;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ru.v0rt3x.perimeter.server.vulnbox.iptables.IPTablesRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VulnBoxUtils {

    private static final Pattern IPTABLES_RULE_PATTERN = Pattern.compile(
        "^(?<target>[^\\s]+)\\s+(?<prot>[^\\s]+)\\s+(?<opt>[^\\s]+)\\s+(?<source>[^\\s]+)\\s+(?<destination>[^\\s]+)\\s*(?<extra>[^\\s].*)?$"
    );

    public static List<IPTablesRule> listIPTablesRules(Session session, OutputStream error, String table) throws IOException, JSchException {
        return VulnBoxUtils.executeCommand(session, error, String.format("sudo iptables -t %s -xnL", table)).stream()
            .map(IPTABLES_RULE_PATTERN::matcher)
            .filter(Matcher::matches)
            .map(matcher -> new IPTablesRule(
                matcher.group("target"), matcher.group("prot"),
                matcher.group("opt"), matcher.group("source"),
                matcher.group("destination"), matcher.group("extra")
            )).collect(Collectors.toList());
    }

    public static Map<String, List<IPTablesRule>> detectLXCServices(Session session, OutputStream error) throws IOException, JSchException {
        Map<String, List<IPTablesRule>> services = new HashMap<>();
        List<IPTablesRule> rules = listIPTablesRules(session, error, "nat");

        if (isCommandPresent(session, error, "lxc")) {
            VulnBoxUtils.executeCommand(session, error, "lxc ls -c n,4 --format csv").stream()
                .map(line -> line.split(",", 2))
                .forEach(container -> {
                    List<IPTablesRule> serviceRules = rules.stream()
                        .filter(rule -> Objects.nonNull(rule.getExtra()) && rule.getExtra().contains(container[1]))
                        .collect(Collectors.toList());

                    services.put(container[0], serviceRules);
                });
        }
        return services;
    }

    public static Map<String, List<IPTablesRule>> detectDockerServices(Session session, OutputStream error) throws IOException, JSchException {
        Map<String, List<IPTablesRule>> services = new HashMap<>();
        List<IPTablesRule> rules = listIPTablesRules(session, error, "nat");

        if (VulnBoxUtils.isCommandPresent(session, error, "docker")) {
            Map<String, String> containers = VulnBoxUtils.executeCommand(session, error, "docker ps --format '{{ .ID }},{{ .Names }}'").stream()
                .map(line -> line.split(",", 2))
                .collect(Collectors.toMap(x -> x[0], x -> x[1]));

            for (String id: containers.keySet()) {
                String ip = VulnBoxUtils.executeCommand(
                    session, error, String.format("docker inspect --format '{{ .NetworkSettings.IPAddress }}' %s", id)
                ).get(0);

                List<IPTablesRule> serviceRules = rules.stream()
                    .filter(rule -> (Objects.nonNull(rule.getExtra()) && rule.getExtra().contains(ip)) || (rule.getDestination().contains(ip)))
                    .collect(Collectors.toList());

                services.put(containers.get(id), serviceRules);
            }
        }
        return services;
    }

    public static boolean isCommandPresent(Session session, OutputStream errorStream, String command) throws IOException, JSchException {
        List<String> output = executeCommand(session, errorStream, String.format("which %s", command));

        if (output.size() >= 1) {
            return output.get(0).contains(command);
        }

        return false;
    }

    public static List<String> executeCommand(Session session, OutputStream errorStream, String command) throws IOException, JSchException {
        Channel execChannel = session.openChannel("exec");

        execChannel.setInputStream(null);
        ((ChannelExec) execChannel).setErrStream(errorStream);

        ((ChannelExec) execChannel).setCommand(command);

        BufferedReader reader = new BufferedReader(new InputStreamReader(execChannel.getInputStream()));
        execChannel.connect();

        List<String> output = new ArrayList<>();

        String line = null;
        while (!execChannel.isClosed() || Objects.nonNull(line)) {
            line = reader.readLine();

            if (Objects.nonNull(line))
                output.add(line);

            sleep(100L);
        }

        execChannel.disconnect();

        return output;
    }

    private static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
