package ru.v0rt3x.perimeter.server.git.validation;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.git.dao.GitRepo;
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.service.validation.ServicePostSubmitAction;
import ru.v0rt3x.perimeter.server.service.validation.ServicePreCommitValidator;
import ru.v0rt3x.perimeter.server.utils.GitUtils;

import java.io.IOException;
import java.util.Objects;

@Component
public class GitValidationManager {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private ConfigurableApplicationContext context;

    private static final Logger logger = LoggerFactory.getLogger(GitValidationManager.class);

    public void preCommit(GitRepo repo, ReceivePack rp, ReceiveCommand cmd) {
        try {
            RevObject revObject = GitUtils.getRevObject(rp, cmd);

            if (revObject instanceof RevCommit) {
                Service service = serviceManager.getService(repo.getName());

                GitPreCommitValidator validator;
                if (Objects.nonNull(service)) {
                    validator = new ServicePreCommitValidator(context, service);
                } else {
                    // TODO: Add support for non-service repository validations
                    validator = null;
                }

                if (Objects.nonNull(validator)) {
                    validator.validate(cmd, (RevCommit) revObject);
                }
            }
        } catch (IOException e) {
            logger.warn("Unable to execute pre-commit validation for '{}': {}", repo.getName(), e.getMessage());
        }
    }

    public void postSubmit(GitRepo repo, ReceivePack rp, ReceiveCommand cmd) {
        try {
            RevObject revObject = GitUtils.getRevObject(rp, cmd);

            if (revObject instanceof RevCommit) {
                Service service = serviceManager.getService(repo.getName());

                GitPostSubmitAction postSubmitAction;
                if (Objects.nonNull(service)) {
                    postSubmitAction = new ServicePostSubmitAction(context, service);
                } else {
                    // TODO: Add support for non-service repository post submit actions
                    postSubmitAction = null;
                }

                if (Objects.nonNull(postSubmitAction)) {
                    postSubmitAction.postSubmit(cmd, (RevCommit) revObject);
                }
            }
        } catch (IOException e) {
            logger.warn("Unable to execute post submit action for '{}': {}", repo.getName(), e.getMessage());
        }
    }
}
