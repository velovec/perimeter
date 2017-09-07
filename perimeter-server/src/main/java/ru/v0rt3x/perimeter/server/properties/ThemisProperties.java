package ru.v0rt3x.perimeter.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "themis")
public class ThemisProperties {

    private String protocol = "http";
    private String host = "localhost";
    private Integer port = 80;
    private boolean integrationEnabled = true;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isIntegrationEnabled() {
        return integrationEnabled;
    }

    public void setIntegrationEnabled(boolean integrationEnabled) {
        this.integrationEnabled = integrationEnabled;
    }
}
