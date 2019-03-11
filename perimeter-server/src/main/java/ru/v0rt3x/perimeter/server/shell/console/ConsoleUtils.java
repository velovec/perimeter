package ru.v0rt3x.perimeter.server.shell.console;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ConsoleUtils {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final OutputStream errorStream;

    private final InputStreamReader inputStreamReader;
    private final OutputStreamWriter outputStreamWriter;
    private final OutputStreamWriter errorStreamWriter;

    private InterruptHandler interruptHandler;

    private final Semaphore writeLock = new Semaphore(1);

    private final List<String> autoCompleteList = new ArrayList<>();

    private final List<String> commandHistory = new ArrayList<>();
    private Integer commandHistoryID = -1;
    private String lastCommand = null;

    public ConsoleUtils(InputStream in, OutputStream out, OutputStream err) {
        inputStream = in;
        outputStream = out;
        errorStream = err;

        inputStreamReader = new InputStreamReader(in);
        outputStreamWriter = new OutputStreamWriter(out);
        errorStreamWriter = new OutputStreamWriter(err);
    }

    private void lock() {
        try {
            writeLock.acquire();
        } catch (InterruptedException ignored) {}
    }

    private void unlock() {
        writeLock.release();
    }

    public void addCompletion(String completion) {
        autoCompleteList.add(completion);
    }

    public void addCompletions(List<String> completions) {
        autoCompleteList.addAll(completions);
    }

    public void addHistoryItem(String historyRecord) {
        commandHistory.add(historyRecord);
        commandHistoryID = -1;
    }

    public void setInterruptHandler(InterruptHandler handler) {
        interruptHandler = handler;
    }

    private List<String> autoComplete(String inputString) {
        return autoCompleteList.stream()
                .filter(completeName -> completeName.startsWith(inputString))
                .collect(Collectors.toList());
    }

    public String readLine() throws IOException {
        return (new BufferedReader(inputStreamReader)).readLine();
    }

    public String read() throws IOException {
        return read("", false);
    }

    public String read(String query) throws IOException {
        return read(query, false);
    }

    public String readPassword(String query) throws IOException {
        return read(query, true);
    }

    public boolean readYesNo(String query) throws IOException {
        while (true) {
            String result = read(String.format(
                "%1$s (y/N): ", query
            ), false);

            switch (result.toLowerCase()) {
                case "y":
                case "yes":
                    return true;
                case "n":
                case "no":
                    return false;
            }
        }
    }

    private String read(String query, boolean secret) throws IOException {
        StringBuilder inputData = new StringBuilder();
        char[] charBuffer = new char[1];

        boolean inputInProgress = true;
        int cursorPosition = 0;

        write(query);

        while (inputInProgress) {
            if (inputStreamReader.read(charBuffer) == 0)
                break;

            switch (charBuffer[0]) {
                case '\r':
                case '\n':
                    inputInProgress = false;
                    newLine();
                    break;
                case '\t':
                    List<String> autoCompleteList = autoComplete(inputData.toString());
                    if (autoCompleteList.size() == 1) {
                        delete(cursorPosition);
                        inputData = new StringBuilder(autoCompleteList.get(0));
                        cursorPosition = inputData.length();
                        write(secret ? secret(inputData.length()) : inputData.toString());
                    } else if (autoCompleteList.size() > 1) {
                        newLine();
                        for (String commandName: autoCompleteList) {
                            writeLine("\t%1$s", commandName);
                        }
                        write(query);
                        write(secret ? secret(inputData.length()) : inputData.toString());
                    }
                    break;
                case '\b':
                case (char)0x7F:
                    if (inputData.length() > 0) {
                        moveCarriage(inputData.length() - cursorPosition);
                        delete(inputData.length());

                        inputData.deleteCharAt(cursorPosition - 1);
                        cursorPosition--;

                        write(secret ? secret(inputData.length()) : inputData.toString());
                        moveCarriage(cursorPosition - inputData.length());
                    }
                    break;
                case (char)0x03:
                    if (interruptHandler != null) interruptHandler.onETXEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x04:
                    if (interruptHandler != null) interruptHandler.onEOTEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x1A:
                    if (interruptHandler != null) interruptHandler.onSUBEvent();
                    inputInProgress = false;
                    write("\r\n");
                    break;
                case (char)0x1B:
                    char[] subCharBuffer = new char[2];
                    if (inputStreamReader.read(subCharBuffer) == 0)
                        continue;
                    if (subCharBuffer[0] == (char)0x5B) {
                        switch (subCharBuffer[1]) {
                            case (char)0x44: // Left Arrow
                                if (cursorPosition > 0) {
                                    cursorPosition--;
                                    moveCarriage(-1);
                                }
                                break;
                            case (char)0x43: // Right Arrow
                                if (cursorPosition < inputData.length()) {
                                    cursorPosition++;
                                    moveCarriage(1);
                                }
                                break;
                            case (char)0x42: // Down Arrow
                                if (commandHistoryID != -1) {
                                    if (commandHistoryID < commandHistory.size() - 1) {
                                        commandHistoryID++;
                                        inputData = new StringBuilder(commandHistory.get(commandHistoryID));
                                    } else {
                                        inputData = new StringBuilder(lastCommand);
                                        commandHistoryID = -1;
                                        lastCommand = null;
                                    }
                                }

                                delete(cursorPosition);
                                write(secret ? secret(inputData.length()) : inputData.toString());
                                cursorPosition = inputData.length();
                                break;
                            case (char)0x41: // Up Arrow
                                if (commandHistoryID == -1) {
                                    lastCommand = inputData.toString();
                                    commandHistoryID = commandHistory.size();
                                }

                                if (commandHistoryID > 0) {
                                    commandHistoryID--;
                                    inputData = new StringBuilder(commandHistory.get(commandHistoryID));

                                    delete(cursorPosition);
                                    write(secret ? secret(inputData.length()) : inputData.toString());
                                    cursorPosition = inputData.length();
                                }
                                break;
                        }
                    }
                    break;
                default:
                    delete(cursorPosition);
                    inputData.insert(cursorPosition, charBuffer);
                    cursorPosition++;
                    write(secret ? secret(inputData.length()) : inputData.toString());
                    moveCarriage(cursorPosition - inputData.length());
                    break;
            }
        }

        newLine();
        return inputData.toString();
    }

    private String secret(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    private void delete(int positions) throws IOException {
        for(int position = 0; position < positions; position++)
            write("\b \b");
    }

    private void moveCarriage(int positions) throws IOException {
        for (int position = 0; position < Math.abs(positions); position++) {
            write(String.valueOf((positions > 0) ? new char[]{0x1B, 0x5B, 0x43} : new char[]{0x1B, 0x5B, 0x44}));
        }
    }

    private String generateBorder(Integer[] fields) {
        List<String> borders = new ArrayList<>();

        for (Integer field: fields) {
            StringBuilder border = new StringBuilder();
            for (int i = 0; i < field + 2; i++) {
                border.append("-");
            }
            borders.add(border.toString());
        }

        return "+" + String.join("+", borders) + "+";
    }

    /**
     * @deprecated use {@link #write(Table)}} instead.
     */
    @Deprecated
    public void write(Map<?, ?> mapObject) throws IOException {
        write(mapObject, null, null);
    }

    /**
     * @deprecated use {@link #write(Table)}} instead.
     */
    @Deprecated
    public void write(Map<?, ?> mapObject, String keyHeader, String valueHeader) throws IOException {
        Table mapTable = new Table(mapObject, keyHeader, valueHeader);

        write(mapTable);
    }

    /**
     * @deprecated use {@link #write(Table)}} instead.
     */
    @Deprecated
    public void write(List<String> list, String header) throws IOException {
        Table listTable = new Table(list, header);

        write(listTable);
    }

    public void write(Table table) throws IOException {
        if (table != null) {
            Integer[] fieldSize = table.getFieldSize();
            String[][] rows = table.asArray();

            String formatString = "| %-" + Arrays.stream(fieldSize).map(Object::toString).collect(Collectors.joining("s | %-")) + "s |";
            String borderString = generateBorder(fieldSize);

            writeLine(borderString);
            writeLine(formatString, (Object[]) rows[0]);
            writeLine(borderString);

            for (int i = 1; i < rows.length; i++) {
                writeLine(formatString, (Object[]) rows[i]);
            }

            writeLine(borderString);
        }
    }

    public void write(Throwable exc) throws IOException {
        writeLine("Traceback (most recent call last):");

        List<StackTraceElement> stackTrace = Arrays.asList(exc.getStackTrace());
        Collections.reverse(stackTrace);

        for (StackTraceElement st: stackTrace) {
            writeLine(
                "  File \"%s\", line %d. in %s",
                st.getFileName().replace(".java", ".py"),
                st.getLineNumber(),
                st.getMethodName()
            );
            writeLine("    source code is hidden");
        }
        writeLine("%s: %s", exc.getClass().getSimpleName(), exc.getMessage());
    }

    public void writeLine(String format, Object... args) throws IOException {
        write(format, args);
        newLine();
    }

    public void newLine() throws IOException {
        lock();
        outputStreamWriter.write("\r\n");
        outputStreamWriter.flush();
        unlock();
    }

    public void write(String format, Object... args) throws IOException {
        lock();
        if (format != null) {
            String[] data = String.format(format, args).split("\r?\n");

            for (int i = 0; i < data.length; i++) {
                outputStreamWriter.write(data[i]);
                if (i < data.length - 1) outputStreamWriter.write("\r\n");
            }
            outputStreamWriter.flush();
        }
        unlock();
    }

    public void error(String format, Object... args) throws IOException {
        lock();
        errorStreamWriter.write(String.format(format, args));
        errorStreamWriter.flush();
        unlock();
    }

    public int read(byte[] buffer) throws IOException {
        return inputStream.read(buffer);
    }

    public void error(byte[] buffer) throws IOException {
        lock();
        errorStream.write(buffer);
        errorStream.flush();
        unlock();
    }

    public void write(byte[] buffer) throws IOException {
        lock();
        outputStream.write(buffer);
        outputStream.flush();
        unlock();
    }

    public int read(char[] buffer) throws IOException {
        return inputStreamReader.read(buffer);
    }

    public void write(char[] buffer) throws IOException {
        lock();
        outputStreamWriter.write(buffer);
        outputStreamWriter.flush();
        unlock();
    }
}