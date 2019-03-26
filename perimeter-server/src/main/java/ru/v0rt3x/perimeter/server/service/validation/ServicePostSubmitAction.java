package ru.v0rt3x.perimeter.server.service.validation;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.springframework.context.ConfigurableApplicationContext;

import ru.v0rt3x.perimeter.server.git.validation.GitPostSubmitAction;
import ru.v0rt3x.perimeter.server.service.dao.Service;

public class ServicePostSubmitAction implements GitPostSubmitAction {

    private final Service service;
    private final ConfigurableApplicationContext context;

    public ServicePostSubmitAction(ConfigurableApplicationContext context, Service service) {
        this.context = context;
        this.service = service;
    }

    @Override
    public void postSubmit(ReceiveCommand cmd, RevCommit commit) {
        // TODO: Implement service post submit actions
    }
}
