package ru.v0rt3x.perimeter.configurator.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "perimeter.configurator")
public class PerimeterConfiguratorProperties {

    private HAProxyProperties haproxy = new HAProxyProperties();

    public HAProxyProperties getHaproxy() {
        return haproxy;
    }

    public void setHaproxy(HAProxyProperties haproxy) {
        this.haproxy = haproxy;
    }

    public class HAProxyProperties {

        private String host;
        private String configPath;
        private String applyCommand;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

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
    }
}
