package ru.v0rt3x.perimeter.server.git.command;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import ru.v0rt3x.perimeter.server.git.GitRepositoryManager;
import ru.v0rt3x.perimeter.server.git.dao.GitRepo;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.perimeter.server.shell.console.Table;
import ru.v0rt3x.perimeter.server.utils.GitUtils;

import java.io.IOException;
import java.util.Objects;

@ShellCommand(command = "git", description = "Manage Git repositories")
public class GitRepositoryCommand extends PerimeterShellCommand {

    private GitRepositoryManager repositoryManager;

    @Override
    protected void init() throws IOException {
        repositoryManager = context.getBean(GitRepositoryManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Create Git repository")
    public void create() throws IOException {
        if (args.size() < 1) {
            console.error("git create <name>");
            exit(1);
            return;
        }

        if (repositoryManager.hasRepository(args.get(0))) {
            console.error("Repository '%s' already exists", args.get(0));
            exit(1);
            return;
        }

        try {
            repositoryManager.createRepository(args.get(0));
        } catch (GitAPIException e) {
            console.writeLine("Unable to create repository: %s", e.getMessage());
        }
    }

    @CommandAction("List Git repositories")
    public void list() throws IOException {
        Table repos = new Table("ID", "Name", "Last Commit");

        for (GitRepo gitRepo: repositoryManager.listRepositories()) {
            Repository repository = repositoryManager.getRepository(gitRepo.getName());

            repos.addRow(
                gitRepo.getId(), gitRepo.getName(),
                GitUtils.getCommitLog(repository).next().getShortMessage()
            );
        }

        console.write(repos);
    }

    @CommandAction("Show Git repository info")
    public void show() throws IOException {
        if (args.size() < 1) {
            console.writeLine("git show <repo> [--commits <number of commits>]");
            exit(1);
            return;
        }

        if (!repositoryManager.hasRepository(args.get(0))) {
            console.error("Repository '%s' not exists", args.get(0));
            exit(1);
            return;
        }

        Repository repository = repositoryManager.getRepository(args.get(0));

        console.writeLine("Repository: %s", args.get(0));
        console.newLine();

        Table lastCommits = new Table("ID", "Author", "Commit Message");

        RevWalk revCommits = GitUtils.getCommitLog(repository);
        for (int i = 0; i < Integer.parseInt(kwargs.getOrDefault("commits", "5")); i++) {
            RevCommit commit = revCommits.next();

            if (Objects.isNull(commit))
                break;

            lastCommits.addRow(
                commit.getName().substring(0, 7), commit.getAuthorIdent().getName(), commit.getShortMessage()
            );
        }

        console.write(lastCommits);
    }

    @CommandAction("Delete Git repository")
    public void delete() throws IOException {
        if (args.size() < 1) {
            console.error("git delete <name>");
            exit(1);
            return;
        }

        if (!repositoryManager.hasRepository(args.get(0))) {
            console.error("Repository '%s' not exists", args.get(0));
            exit(1);
            return;
        }

        try {
            repositoryManager.deleteRepository(args.get(0));
        } catch (IOException e) {
            console.writeLine("Unable to delete repository: %s", e.getMessage());
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
