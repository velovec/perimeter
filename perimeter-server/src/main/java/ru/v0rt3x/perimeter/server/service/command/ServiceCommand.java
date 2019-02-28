package ru.v0rt3x.perimeter.server.service.command;

import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "service", description = "Manage services")
public class ServiceCommand extends PerimeterShellCommand {

    private ServiceManager serviceManager;

    @Override
    protected void init() throws IOException {
        serviceManager = context.getBean(ServiceManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List services")
    public void list() throws IOException {
        Table serviceList = new Table("Name", "Port", "Mode", "Status");

        for (Service service: serviceManager.listServices()) {
            serviceList.addRow(service.getName(), service.getPort(), service.getMode(), service.getStatusString());
        }

        console.write(serviceList);
    }

    @Override
    protected void onInterrupt() {

    }
}
