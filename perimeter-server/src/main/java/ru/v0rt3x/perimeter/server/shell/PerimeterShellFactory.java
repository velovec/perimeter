package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.command.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PerimeterShellFactory implements Factory<Command> {

    @Autowired
    private PerimeterShellCommandManager commandManager;

    @Override
    public Command create() {
        return new PerimeterShell(commandManager);
    }
}
