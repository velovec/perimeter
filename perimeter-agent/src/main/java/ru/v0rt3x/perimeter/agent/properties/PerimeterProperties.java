package ru.v0rt3x.perimeter.agent.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.v0rt3x.perimeter.agent.types.AgentType;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "perimeter")
public class PerimeterProperties {

    private AgentProperties agent = new AgentProperties();
    private ServerProperties server = new ServerProperties();
    private ExecutorProperties executor = new ExecutorProperties();
    private MonitorProperties monitor = new MonitorProperties();

    public AgentProperties getAgent() {
        return agent;
    }

    public void setAgent(AgentProperties agent) {
        this.agent = agent;
    }

    public ServerProperties getServer() {
        return server;
    }

    public void setServer(ServerProperties server) {
        this.server = server;
    }

    public ExecutorProperties getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorProperties executor) {
        this.executor = executor;
    }

    public MonitorProperties getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorProperties monitor) {
        this.monitor = monitor;
    }

    public class AgentProperties {

        private AgentType type;

        public AgentType getType() {
            return type;
        }

        public void setType(String type) {
            this.type = AgentType.valueOf(type);
        }
    }

    public class ServerProperties {

        private String host;
        private Integer port;
        private String protocol;

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

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }

    public class ExecutorProperties {

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

    public class MonitorProperties {

        private String gatewayInterface;

        public String getGatewayInterface() {
            return gatewayInterface;
        }

        public void setGatewayInterface(String gatewayInterface) {
            this.gatewayInterface = gatewayInterface;
        }
    }
}
