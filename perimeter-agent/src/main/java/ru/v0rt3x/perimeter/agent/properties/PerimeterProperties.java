package ru.v0rt3x.perimeter.agent.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.v0rt3x.perimeter.agent.types.AgentType;

@ConfigurationProperties(prefix = "perimeter")
public class PerimeterProperties {

    private AgentProperties agent = new AgentProperties();
    private ServerProperties server = new ServerProperties();

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

}
