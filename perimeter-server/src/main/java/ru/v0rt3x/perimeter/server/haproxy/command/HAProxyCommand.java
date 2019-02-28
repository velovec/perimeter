package ru.v0rt3x.perimeter.server.haproxy.command;

import ru.v0rt3x.perimeter.server.agent.AgentManager;
import ru.v0rt3x.perimeter.server.agent.AgentTask;
import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyMapping;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ShellCommand(command = "haproxy", description = "Manage HAProxy configuration")
public class HAProxyCommand extends PerimeterShellCommand {

    private HAProxyManager haProxyManager;

    @Override
    protected void init() throws IOException {
        haProxyManager = context.getBean(HAProxyManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List backend mapping")
    public void list() throws IOException {
        Table backendMapping = new Table("Service", "Backend", "ACL", "Default");

        for (HAProxyMapping mapping: haProxyManager.listMappings()) {
            backendMapping.addRow(
                mapping.getService().getName(), mapping.getBackend().getName(),
                (Objects.nonNull(mapping.getAcl())) ? mapping.getAcl().getName() : "",
                Objects.isNull(mapping.getAcl())
            );
        }

        console.write(backendMapping);
    }

    @CommandAction("Check HAProxy status")
    public void status() throws IOException {
        Table statsTable = new Table("Proxy", "Server", "Status", "Check Status", "Check Code", "Active Servers");
        haProxyManager.getHAProxyStats().stream()
            .filter(statRecord -> !"haproxy-stats".equals(statRecord.get(0)))
            .forEach(statRecord ->statsTable.addRow(
                statRecord.get(0), statRecord.get(1), statRecord.get(17),
                statRecord.get(36), statRecord.get(37), "BACKEND".equals(statRecord.get(1)) ? statRecord.get(19) : ""
            ));
        console.write(statsTable);
    }

    @CommandAction("Apply HAProxy configuration")
    public void apply() throws IOException {
        AgentTask agentTask = new AgentTask();

        Map<Service, List<HAProxyMapping>> serviceMappings = haProxyManager.listMappings().stream()
            .collect(Collectors.groupingBy(HAProxyMapping::getService, Collectors.mapping(x -> x, Collectors.toList())));

        Map<String, Object> configuration = new HashMap<>();
        for (Service service: serviceMappings.keySet()) {
            Map<String, Object> serviceDefinition = new HashMap<>();

            serviceDefinition.put("port", service.getPort());
            serviceDefinition.put("mode", service.getMode());
            serviceDefinition.put("check", service.getCheck());
            serviceDefinition.put("expect", service.getExpect());

            List<Map<String, Object>> mappings = new ArrayList<>();
            for (HAProxyMapping mapping: serviceMappings.get(service)) {
                Map<String, Object> mappingDefinition = new HashMap<>();

                mappingDefinition.put("backend", mapping.getBackend());
                mappingDefinition.put("acl", mapping.getAcl());

                mappings.add(mappingDefinition);
            }

            serviceDefinition.put("mappings", mappings);

            configuration.put(service.getName(), serviceDefinition);
        }

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("type", "haproxy");
        parameters.put("configuration", configuration);

        agentTask.setType("configure");
        agentTask.setParameters(parameters);

        context.getBean(AgentManager.class).queueTask(agentTask);
    }

    @Override
    protected void onInterrupt() {

    }
}
