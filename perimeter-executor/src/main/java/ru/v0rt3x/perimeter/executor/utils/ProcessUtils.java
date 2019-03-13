package ru.v0rt3x.perimeter.executor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static void kill(int processId, int signal) {
        for (String childProcessId: exec("pgrep", "-P", String.valueOf(processId))) {
            kill(Integer.parseInt(childProcessId), signal);
        }

        logger.debug("Killing PID{} with signal {}", processId, signal);
        exec("kill", "-" + signal, String.valueOf(processId));
    }

    public static List<String> exec(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        List<String> output = new ArrayList<>();
        try {
            Process process = processBuilder.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (true) {
                    String line = reader.readLine();

                    if (line == null)
                        break;

                    output.add(line);
                }
            }
        } catch (InterruptedException | IOException e) {
            logger.error("Unable to execute command: {}: {}", command, e.getMessage());
        }

        return output;
    }

    public static int getProcessId(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");

            pidField.setAccessible(true);

            return (int) pidField.get(process);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unsupported operation: getpid");
        }
    }
}
