package ru.v0rt3x.perimeter.server.git.command;

import org.eclipse.jgit.lib.Repository;

import ru.v0rt3x.perimeter.server.git.GitRepositoryManager;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;

import java.io.IOException;
import java.util.Objects;

public abstract class GitPackCommand extends PerimeterShellCommand {

    private GitRepositoryManager repositoryManager;

    @Override
    protected void init() throws IOException {
        repositoryManager = context.getBean(GitRepositoryManager.class);
    }

    @Override
    protected void execute() throws IOException {
        if (args.size() < 1) {
            exit(1);
            return;
        }

        Repository repository = repositoryManager.getRepository(args.get(0));
        if (Objects.isNull(repository)) {
            exit(1);
            return;
        }

        pack(repository);
    }

    protected abstract void pack(Repository repository) throws IOException;

    @Override
    protected void onInterrupt() {

    }
}
