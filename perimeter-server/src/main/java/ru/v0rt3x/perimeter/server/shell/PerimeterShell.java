package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.SignalListener;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.v0rt3x.perimeter.server.shell.command.EmptyCommand;
import ru.v0rt3x.perimeter.server.shell.command.InvalidCommand;
import ru.v0rt3x.perimeter.server.shell.command.UnknownCommand;
import ru.v0rt3x.shell.console.CommandLineParser;
import ru.v0rt3x.shell.console.ConsoleEngine;
import ru.v0rt3x.shell.console.ansi.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PerimeterShell implements Command, Runnable, ExitCallback, InterruptHandler, SignalListener {

    private final PerimeterShellCommandManager commandManager;

    private InputStream input;
    private OutputStream output;
    private OutputStream error;

    private ConsoleEngine console;

    private ExitCallback exitCallback;
    private Environment environment;

    private Thread commandThread;
    private boolean isRunning = true;

    private PerimeterShellCommand subCommand;
    private boolean subCommandIsRunning = false;
    private Integer subCommandExitCode = 0;

    private static final Logger logger = LoggerFactory.getLogger(PerimeterShell.class);

    public PerimeterShell(PerimeterShellCommandManager yggdrasilShellCommandManager) {
        commandManager = yggdrasilShellCommandManager;
    }

    @Override
    public void setInputStream(InputStream inputStreamObject) {
        input = inputStreamObject;
    }

    @Override
    public void setOutputStream(OutputStream outputStreamObject) {
        output = outputStreamObject;
    }

    @Override
    public void setErrorStream(OutputStream errorStreamObject) {
        error = errorStreamObject;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        environment = env;

        environment.addSignalListener(this);

        console = new ConsoleEngine(input, output, error);

        console.addCompletions(commandManager.listCommands());
        console.setInterruptHandler(this);

        commandThread = new Thread(this);
        commandThread.start();

        isRunning = true;
    }

    @Override
    public void destroy() {
        isRunning = false;
        if (subCommand != null) {
            subCommand.destroy();
        }
        commandThread.interrupt();

        environment.removeSignalListener(this);
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                ConsoleColor rcColor = (subCommandExitCode == 0) ? ConsoleColor.BRIGHT_GREEN : ConsoleColor.BRIGHT_RED;
                String userName = environment.getEnv().get("USER");

                String commandLine = console.read(String.format(
                    "%s %s ",
                    ANSIUtils.RenderString("$$$", rcColor, ConsoleTextStyle.BOLD),
                    ANSIUtils.RenderString(userName, ConsoleColor.BRIGHT_CYAN, ConsoleTextStyle.BOLD)
                ));

                console.addHistoryItem(commandLine);

                CommandLineParser.CommandLine command = CommandLineParser.parse(commandLine);

                subCommand = routeCommand(command.getCmd());
                subCommand.setUpCommand(commandManager, command);

                subCommand.setExitCallback(this);

                subCommand.setInputStream(input);
                subCommand.setOutputStream(output);
                subCommand.setErrorStream(error);

                subCommandIsRunning = true;

                subCommand.start(environment);

                try {
                    while (subCommandIsRunning||subCommand.isRunning()) {
                        Thread.sleep(10);
                    }

                    subCommand.destroy();
                } catch (InterruptedException ignored) {}

                output.flush();
                error.flush();

            } catch (IOException e) {
                logger.error("Unable to process ShellRequest: [{}] {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }

        exit(0);
    }

    private PerimeterShellCommand routeCommand(String command) {
        if (command == null) {
            return new InvalidCommand();
        }

        if (command.equals("")) {
            return new EmptyCommand();
        }

        PerimeterShellCommand commandObject = commandManager.getCommand(command);
        if (commandObject != null) {
            return commandObject;
        }

        return new UnknownCommand();
    }

    private void exit(Integer exitCode) {
        exitCallback.onExit(exitCode);
    }

    private boolean isRunning() {
        return isRunning;
    }

    public void onExit(int exitCode) {
        subCommandIsRunning = false;
        subCommandExitCode = exitCode;
    }

    public void onExit(int exitCode, String exitMessage) {
        subCommandIsRunning = false;
        subCommandExitCode = exitCode;

        if (exitMessage.equals("logout")) {
            isRunning = false;
        }
    }

    @Override
    public void onEOTEvent() {
        isRunning = false;
    }

    @Override
    public void onETXEvent() {
        isRunning = false;
    }

    @Override
    public void onSUBEvent() {
        isRunning = false;
    }

    @Override
    public void signal(Signal signal) {
        switch (signal) {
            case INT:
            case KILL:
            case TERM:
            case HUP:
                isRunning = false;
                break;
        }
    }
}