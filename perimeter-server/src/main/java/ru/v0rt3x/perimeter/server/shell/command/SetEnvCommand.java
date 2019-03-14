package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "setenv", description = "Set environment variables")
public class SetEnvCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).contains("=")) {
                String[] arg = args.get(i).split("=", 2);

                kwargs.put(arg[0], arg[1]);
            } else {
                if (i < args.size() - 1) {
                    kwargs.put(args.get(i++), args.get(i));
                } else {
                    getEnvironment().getEnv().remove(args.get(i));
                }
            }
        }

        if (kwargs.containsKey("USER")) {
            kwargs.remove("USER");
            console.writeLine("USER variable cannot be changed");
        }

        getEnvironment().getEnv().putAll(kwargs);
    }

    @Override
    protected void onInterrupt() {}
}