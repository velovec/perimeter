package ru.v0rt3x.perimeter.server.git.command.impl;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import ru.v0rt3x.perimeter.server.git.command.GitPackCommand;
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
        (new UploadPack(repository)).upload(input, output, error);
    }

}
