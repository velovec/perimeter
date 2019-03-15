package ru.v0rt3x.perimeter.server.shell.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineUtils {

    public static CommandLine parse(String commandLine) {
        String command = null;
        String keyword = null;

        commandLine += '\0';

        List<String> positionalArguments = new ArrayList<>();
        Map<String, String> keyWordArguments = new HashMap<>();

        boolean doubleQuotes = false;
        boolean singleQuotes = false;

        boolean commandArgument = true;
        boolean positionalArgument = false;
        boolean keywordArgument = false;
        boolean keywordValue = false;

        boolean escapeCharacter = false;

        StringBuilder currentString = new StringBuilder();

        for (char chr: commandLine.toCharArray()) {
            switch (chr) {
                case '"':
                    doubleQuotes = escapeCharacter == doubleQuotes;
                    currentString.append(escapeCharacter ? "\"" : "");
                    escapeCharacter = false;
                    break;
                case '\'':
                    singleQuotes = escapeCharacter == singleQuotes;
                    escapeCharacter = false;
                    break;
                case '\\':
                    currentString.append(escapeCharacter ? "\\" : "");
                    escapeCharacter = !escapeCharacter;
                    break;
                case '-':
                    if (commandArgument||positionalArgument||keywordArgument) {
                        if (currentString.length() > 0)
                            currentString.append("-");
                    } else if (keywordValue) {
                        if (currentString.length() > 0) {
                            currentString.append("-");
                        } else {
                            keyWordArguments.put(currentString.toString(), "");
                            currentString = new StringBuilder();
                            keywordArgument = true;
                            keywordValue = false;
                        }
                    } else {
                        keywordArgument = true;
                    }
                    break;
                case ' ':
                    if (commandArgument) {
                        if (currentString.length() > 0) {
                            command = currentString.toString();
                            currentString = new StringBuilder();
                            commandArgument = false;
                        }
                    } else if (doubleQuotes||singleQuotes) {
                        if (!(positionalArgument||keywordArgument||keywordValue))
                            positionalArgument = true;
                        currentString.append(' ');
                    } else {
                        if (positionalArgument) {
                            positionalArguments.add(currentString.toString());
                            currentString = new StringBuilder();
                            positionalArgument = false;
                        } else if (keywordArgument) {
                            keyword = currentString.toString();
                            currentString = new StringBuilder();
                            keywordArgument = false;
                            keywordValue = true;
                        } else if (keywordValue) {
                            keyWordArguments.put(keyword, currentString.toString());
                            currentString = new StringBuilder();
                            keywordValue = false;
                        }
                    }
                    break;
                case '=':
                    if (keywordArgument) {
                        keyword = currentString.toString();
                        currentString = new StringBuilder();
                        keywordArgument = false;
                        keywordValue = true;
                    } else {
                        currentString.append('=');
                    }
                    break;
                case '\0':
                    if (commandArgument) {
                        command = currentString.toString();
                    } else if (keywordArgument) {
                        keyWordArguments.put(currentString.toString(), "");
                    } else if (keywordValue) {
                        keyWordArguments.put(keyword, currentString.toString());
                    } else if (positionalArgument) {
                        positionalArguments.add(currentString.toString());
                    }
                    break;
                default:
                    if (!(commandArgument||positionalArgument||keywordArgument||keywordValue))
                        positionalArgument = true;
                    currentString.append(chr);
                    break;
            }
        }

        return new CommandLine(command, positionalArguments, keyWordArguments);
    }

    public static class CommandLine {

        private final String cmd;
        private final List<String> args;
        private final Map<String, String> kwargs;

        private CommandLine(String command, List<String> positionalArguments, Map<String, String> keyWordArguments) {
            cmd = command;
            args = positionalArguments;
            kwargs = keyWordArguments;
        }

        public String getCmd() {
            return cmd;
        }

        public List<String> getArgs() {
            return args;
        }

        public Map<String, String> getKeywordArgs() {
            return kwargs;
        }

        @Override
        public String toString() {
            return String.format("Command: %s Args: %s KWArgs: %s", cmd, args, kwargs);
        }
    }
}