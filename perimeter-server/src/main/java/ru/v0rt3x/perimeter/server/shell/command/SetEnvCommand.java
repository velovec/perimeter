package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "setenv", description = "Set environment variables")
public class SetEnvCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    @Override
    protected void execute() throws IOException {
        console.writeLine("A: %s K: %s", args, kwargs);
        for (int i = 0; i < args.size(); i++) {
            String argument = args.get(i);

            if (argument.contains("=")) {
                String[] arg = args.get(i).split("=", 2);

                setEnv(arg[0], arg[1]);
                console.writeLine("%s; %s", arg[0], arg[1]);
            } else {
                if (i + 1 < args.size()) {
                    String value = args.get(i + 1);
                    setEnv(argument, value);
                    console.writeLine("%s; %s", argument, value);
                    i++;
                } else {
                    console.error("Not enough arguments");
                    exit(1);
                    return;
                }
            }
        }

        setEnv(kwargs);

        console.write(new Table(getEnv(), "variable", "value"));
    }

    @Override
    protected void onInterrupt() {}
}