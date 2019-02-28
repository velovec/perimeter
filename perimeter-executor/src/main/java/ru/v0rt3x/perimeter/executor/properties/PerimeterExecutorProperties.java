package ru.v0rt3x.perimeter.executor.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "perimeter.executor")
public class PerimeterExecutorProperties {

    private Map<String, String> commandLine = new LinkedHashMap<>();

    private File tmpDirectory;

    private Long executionTimeout;

    public File getTmpDirectory() {
        return tmpDirectory;
    }

    public void setTmpDirectory(String tmpDirectory) {
        this.tmpDirectory = new File(tmpDirectory);
    }

    public Map<String, String> getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(Map<String, String> commandLine) {
        this.commandLine = commandLine;
    }

    public Long getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(Long executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
}
