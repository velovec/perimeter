package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.files.FileRouter;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ShellCommand(command = "scp", description = "Copy files over SSH")
public class SCPCommand extends PerimeterShellCommand {

    private FileRouter fileRouter;

    @Override
    protected void init() throws IOException {
        fileRouter = context.getBean(FileRouter.class);
    }

    @Override
    protected void execute() throws IOException {
        if (kwargs.containsKey("t")) {
            String targetName = kwargs.get("t");

            console.write(new byte[] {0});

            char[] op = new char[1];
            if (console.read(op) == 0)
                return;

            switch (op[0]) {
                case 'T':
                    console.readLine();
                    break;
                case 'C':
                    String[] fileHeader = console.readLine().split(" ");

                    console.write(new byte[] {0});

                    int fileSize = Integer.parseInt(fileHeader[1]);

                    ByteArrayOutputStream fileContents = new ByteArrayOutputStream();

                    int totalRead = 0;

                    int bytesRead;
                    byte[] buffer = new byte[1024];

                    while (totalRead < fileSize) {
                        bytesRead = console.read(buffer);
                        fileContents.write(buffer, 0, bytesRead - 1);
                        totalRead += bytesRead;
                    }

                    console.write(new byte[] {0});

                    if (!fileRouter.routeFile(getEnvironment().getEnv().get("USER"), targetName, fileContents.toByteArray())) {
                        logger.warn("No route for file '{}' found", targetName);
                    }
                    break;
                case 'D':
                    // No directory copying support required
                    exit(1);
                    break;
                case 'E':
                    // No directory copying support required
                    exit(1);
                    break;
            }
        }
    }

    @Override
    protected void onInterrupt() {

    }
}