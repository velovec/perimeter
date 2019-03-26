package ru.v0rt3x.perimeter.server.service.validation;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.springframework.context.ConfigurableApplicationContext;

import ru.v0rt3x.perimeter.server.git.validation.GitPreCommitValidator;
import ru.v0rt3x.perimeter.server.service.dao.Service;

public class ServicePreCommitValidator implements GitPreCommitValidator {

    private final Service service;
    private final ConfigurableApplicationContext context;

    public ServicePreCommitValidator(ConfigurableApplicationContext context, Service service) {
        this.context = context;
        this.service = service;
    }

    @Override
    public void validate(ReceiveCommand cmd, RevCommit revCommit) {
        switch (cmd.getType()) {
            case CREATE:
                if (!cmd.getRefName().equals("master"))
                    cmd.setResult(ReceiveCommand.Result.REJECTED_NOCREATE);
                break;
            case UPDATE:
                break;
            case UPDATE_NONFASTFORWARD:
                cmd.setResult(ReceiveCommand.Result.REJECTED_NONFASTFORWARD);
                break;
            case DELETE:
                cmd.setResult(ReceiveCommand.Result.REJECTED_NODELETE);
                break;
        }

        // TODO: Implement pre-commit validation
    }
}
