package ru.v0rt3x.shell.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table {

    private final String[] header;
    private final List<String[]> rows = new ArrayList<>();
    private final Integer[] fieldSize;

    public Table(String... columns) {
        fieldSize = calculateHeaderSize(columns);
        header = columns;
    }

    public Table(List<String> columns) {
        this ((String[]) columns.toArray());
    }

    public Table(Map<?, ?> values, String keyHeader, String valueHeader) {
        this((keyHeader != null) ? keyHeader : "", (valueHeader != null) ? valueHeader : "");

        for (Object key: values.keySet()) {
            addRow(key, values.get(key));
        }
    }

    public Table(List<String> values, String fieldHeader) {
        this(fieldHeader);

        values.forEach(this::addRow);
    }

    private Integer[] calculateHeaderSize(String[] header) {
        Integer[] fieldSize = new Integer[header.length];
        for (int i = 0; i < header.length; i++) {
            fieldSize[i] = header[i].length();
        }

        return fieldSize;
    }

    public void addRow(Object... values) {
        if (values.length != header.length) {
            throw new IllegalArgumentException("Row size do not match header size");
        }

        String[] stringValues = new String[values.length];
        for (int i = 0; i < header.length; i++) {
            stringValues[i] = String.valueOf(values[i]);
            fieldSize[i] = Math.max(fieldSize[i], stringValues[i].length());
        }

        rows.add(stringValues);
    }

    public Integer[] getFieldSize() {
        return fieldSize;
    }

    public String[][] asArray() {
        String[][] array = new String[rows.size() + 1][];

        array[0] = header;

        int arrayIndex = 1;
        for (String[] row: rows) {
            array[arrayIndex] = row;

            arrayIndex++;
        }

        return array;
    }
}