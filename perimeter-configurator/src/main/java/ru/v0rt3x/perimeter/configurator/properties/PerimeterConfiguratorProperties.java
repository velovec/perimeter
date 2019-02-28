package ru.v0rt3x.perimeter.configurator.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "perimeter.configurator")
public class PerimeterConfiguratorProperties {

    private String templatesPath;
    private Map<String, Configurator> configurators;

    public Map<String, Configurator> getConfigurators() {
        return configurators;
    }

    public Configurator getConfigurator(String name) {
        return configurators.get(name);
    }

    public void setConfigurators(Map<String, Configurator> configurators) {
        this.configurators = configurators;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }
}
