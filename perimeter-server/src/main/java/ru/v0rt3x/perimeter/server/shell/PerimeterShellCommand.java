package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.SignalListener;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.CommandLineParser;
import ru.v0rt3x.shell.console.ConsoleEngine;
import ru.v0rt3x.shell.console.Table;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class PerimeterShellCommand implements Command, Runnable, InterruptHandler, SignalListener {

    protected InputStream input;
    protected OutputStream output;
    protected OutputStream error;

    protected InputStreamReader inputReader;
    protected OutputStreamWriter outputWriter;
    protected OutputStreamWriter errorWriter;

    protected ConsoleEngine console;

    private ExitCallback exitCallback;
    private Environment environment;

    private Thread commandThread;

    protected String command;
    protected List<String> args;
    protected Map<String, String> kwargs;

    private volatile boolean isRunning = true;
    private volatile boolean exited = false;

    protected PerimeterShellCommandManager commandManager;
    protected ConfigurableApplicationContext context;

    protected static final Logger logger = LoggerFactory.getLogger(PerimeterShellCommand.class);

    private static final String DEFAULT_METHOD = "execute";

    public void setUpCommand(PerimeterShellCommandManager cmdManager, CommandLineParser.CommandLine commandObject) {
        args = commandObject.getArgs();
        kwargs = commandObject.getKeywordArgs();
        command = commandObject.getCmd();

        commandManager = cmdManager;
        context = cmdManager.getContext();
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        input = inputStream;
        inputReader = new InputStreamReader(inputStream);
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        output = outputStream;
        outputWriter = new OutputStreamWriter(outputStream);
    }

    @Override
    public void setErrorStream(OutputStream errorStream) {
        error = errorStream;
        errorWriter = new OutputStreamWriter(errorStream);
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        exitCallback = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        environment = env;

        commandThread = new Thread(this);
        commandThread.start();

        isRunning = true;

        environment.addSignalListener(this);
    }

    @Override
    public void destroy() {
        isRunning = false;
        onInterrupt();
        commandThread.interrupt();

        environment.removeSignalListener(this);
    }

    @Override
    public void run() {
        try {
            console = new ConsoleEngine(input, output, error);

            init();

            String action = null;
            if ((args.size() > 0) && hasMethod(args.get(0))) {
                action = args.get(0);
                args.remove(0);
            }

            dispatch(action);
        } catch (IOException e) {
            logger.error("Unable to execute command", e);
            exit(1);
        }

        if (!exited) {
            exited = true;
            exit(0);
        }

        isRunning = false;
    }

    private boolean hasMethod(String name) {
        return Arrays.stream(getClass().getDeclaredMethods())
            .anyMatch(method -> method.getName().equals(name));
    }

    protected abstract void init() throws IOException;
    protected abstract void execute() throws IOException;
    protected abstract void onInterrupt();

    protected void sleep(Long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void exit(Integer exitCode) {
        exitCallback.onExit(exitCode);
        exited = true;
    }

    protected void exit(Integer exitCode, String exitMessage) {
        exitCallback.onExit(exitCode, exitMessage);
        exited = true;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected Map<String, String> getEnv() {
        return environment.getEnv();
    }

    protected String getEnv(String key) {
        return environment.getEnv().get(key);
    }

    protected String getEnv(String key, String defaultValue) {
        return environment.getEnv().getOrDefault(key, defaultValue);
    }

    protected void setEnv(String key, String value) {
        environment.getEnv().put(key, value);
    }

    protected void setEnv(Map<String, String> env) {
        for (String immutableKey: new String[] {"USER", "LINES", "COLUMNS"}) {
            env.remove(immutableKey);
        }

        environment.getEnv().putAll(env);
    }

    protected boolean isRunning() {
        return isRunning;
    }

    private void dispatch(String methodName) throws IOException {
        if (methodName == null)
            methodName = DEFAULT_METHOD;

        try {
            Method handler = getClass().getDeclaredMethod(methodName);
            handler.setAccessible(true);

            handler.invoke(this);
        } catch (NoSuchMethodException e) {
            if (!DEFAULT_METHOD.equals(methodName)) {
                dispatch(DEFAULT_METHOD);
            } else {
                console.writeLine("Unable to execute command");
            }
        } catch (InvocationTargetException e) {
            if (NotImplementedException.class.isAssignableFrom(e.getCause().getClass())) {
                Table commandMethods = new Table("Action", "Description");
                for (Method method: getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(CommandAction.class)) {
                        commandMethods.addRow(method.getName(), method.getAnnotation(CommandAction.class).value());
                    }
                }
                console.write(commandMethods);
            } else {
                Throwable t = e.getCause();

                console.writeLine("Unable to execute command: (%s) %s", t.getClass().getSimpleName(), t.getMessage());
                console.write(t);
            }
        } catch (IllegalAccessException e) {
            console.writeLine("Unable to perform action '%s': %s", methodName, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void onEOTEvent() {
        destroy();
    }

    @Override
    public void onETXEvent() {
        destroy();
    }

    @Override
    public void onSUBEvent() {
        destroy();
    }

    @Override
    public void signal(Signal signal) {
        switch (signal) {
            case INT:
            case KILL:
            case TERM:
            case HUP:
                destroy();
        }
    }
}