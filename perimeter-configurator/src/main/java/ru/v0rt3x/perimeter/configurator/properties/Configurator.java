package ru.v0rt3x.perimeter.configurator.properties;

import java.util.Map;

public class Configurator {

    private String configPath;
    private String applyCommand;
    private String templateFile;
    private Map<String, Object> overrides;

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getApplyCommand() {
        return applyCommand;
    }

    public void setApplyCommand(String applyCommand) {
        this.applyCommand = applyCommand;
    }

    public Map<String, Object> getOverrides() {
        return overrides;
    }

    public void setOverrides(Map<String, Object> overrides) {
        this.overrides = overrides;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
}
