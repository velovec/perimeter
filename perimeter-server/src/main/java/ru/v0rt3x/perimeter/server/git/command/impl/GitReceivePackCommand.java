package ru.v0rt3x.perimeter.server.git.command.impl;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;

import ru.v0rt3x.perimeter.server.git.command.GitPackCommand;
import ru.v0rt3x.perimeter.server.git.hooks.GitPostReceiveHook;
import ru.v0rt3x.perimeter.server.git.hooks.GitPreReceiveHook;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "git-receive-pack", description = "(INTERNAL) Git receive-pack")
public class GitReceivePackCommand extends GitPackCommand {

    @Override
    protected void execute() throws IOException {
        super.execute();
    }

    @Override
    protected void pack(Repository repository) throws IOException {
        ReceivePack receivePack = new ReceivePack(repository);

        receivePack.setPreReceiveHook(context.getBean(GitPreReceiveHook.class));
        receivePack.setPostReceiveHook(context.getBean(GitPostReceiveHook.class));

        receivePack.receive(input, output, error);
    }

}