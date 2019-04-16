package ru.v0rt3x.perimeter.server.service.command;

import org.eclipse.jgit.lib.Repository;
import ru.v0rt3x.perimeter.server.git.GitRepositoryManager;
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;
import ru.v0rt3x.perimeter.server.utils.GitUtils;

import java.io.IOException;
import java.util.Objects;

@ShellCommand(command = "service", description = "Manage services")
public class ServiceCommand extends PerimeterShellCommand {

    private ServiceManager serviceManager;
    private GitRepositoryManager repositoryManager;

    @Override
    protected void init() throws IOException {
        serviceManager = context.getBean(ServiceManager.class);
        repositoryManager = context.getBean(GitRepositoryManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List services")
    public void list() throws IOException {
        Table serviceList = new Table("Name", "Port", "Mode", "Status", "Last Commit");

        for (Service service: serviceManager.listServices()) {
            Repository repository = repositoryManager.getRepository(service.getName());

            serviceList.addRow(
                service.getName(), service.getPort(), service.getMode(), service.getStatusString(),
                Objects.nonNull(repository) ? GitUtils.getCommitLog(repository).next().getShortMessage() : "No Git repository"
            );
        }

        console.write(serviceList);
    }

    @CommandAction("Deploy service from Git")
    public void deploy() throws IOException {
        if (args.size() < 1) {
            console.error("service deploy <service>");
            exit(1);
            return;
        }

        Service service = serviceManager.getService(args.get(0));
        if (Objects.isNull(service)) {
            console.error("Service '{}' doesn't exists", args.get(0));
            exit(1);
            return;
        }

        if (!repositoryManager.hasRepository(service.getName())) {
            console.error("Service '{}' has no Git repository", args.get(0));
            exit(1);
            return;
        }

        if (Objects.isNull(service.getDeployScript()) || service.getDeployScript().length() == 0) {
            console.error("Service '{}' has no deployment script", args.get(0));
            exit(1);
            return;
        }

        console.writeLine("Not implemented yet");
    }

    @Override
    protected void onInterrupt() {

    }
}
