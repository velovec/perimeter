package ru.v0rt3x.perimeter.server.git.hooks;

import org.eclipse.jgit.transport.PreReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.git.GitRepositoryManager;
import ru.v0rt3x.perimeter.server.git.dao.GitRepo;
import ru.v0rt3x.perimeter.server.git.validation.GitValidationManager;

import java.util.Collection;

@Component
public class GitPreReceiveHook implements PreReceiveHook {

    @Autowired
    private GitValidationManager validationManager;

    @Autowired
    private GitRepositoryManager repositoryManager;

    @Override
    public void onPreReceive(ReceivePack rp, Collection<ReceiveCommand> commands) {
        String repoName = rp.getRepository().getDirectory().getName();

        if (repositoryManager.hasRepository(repoName)) {
            GitRepo gitRepo = repositoryManager.getGitRepository(repoName);

            for (ReceiveCommand cmd: commands) {
                validationManager.preCommit(gitRepo, rp, cmd);
            }
        }
    }
}
