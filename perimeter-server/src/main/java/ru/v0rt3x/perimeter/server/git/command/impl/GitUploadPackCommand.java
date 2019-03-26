package ru.v0rt3x.perimeter.server.git.command.impl;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;

import ru.v0rt3x.perimeter.server.git.command.GitPackCommand;
import ru.v0rt3x.perimeter.server.git.hooks.GitPostUploadHook;
import ru.v0rt3x.perimeter.server.git.hooks.GitPreUploadHook;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "git-upload-pack", description = "(INTERNAL) Git upload-pack")
public class GitUploadPackCommand extends GitPackCommand {

    @Override
    protected void execute() throws IOException {
        super.execute();
    }

    @Override
    protected void pack(Repository repository) throws IOException {
        UploadPack uploadPack = new UploadPack(repository);

        uploadPack.setPreUploadHook(context.getBean(GitPreUploadHook.class));
        uploadPack.setPostUploadHook(context.getBean(GitPostUploadHook.class));

        uploadPack.upload(input, output, error);
    }

}
