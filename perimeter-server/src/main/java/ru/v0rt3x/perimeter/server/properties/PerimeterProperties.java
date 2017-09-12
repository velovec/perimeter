package ru.v0rt3x.perimeter.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "perimeter")
public class PerimeterProperties {

    private TeamProperties team = new TeamProperties();
    private FlagProperties flag = new FlagProperties();
    private ThemisProperties themis = new ThemisProperties();
    private AgentProperties agent = new AgentProperties();

    public TeamProperties getTeam() {
        return team;
    }

    public void setTeam(TeamProperties team) {
        this.team = team;
    }

    public FlagProperties getFlag() {
        return flag;
    }

    public void setFlag(FlagProperties flag) {
        this.flag = flag;
    }

    public ThemisProperties getThemis() {
        return themis;
    }

    public void setThemis(ThemisProperties themis) {
        this.themis = themis;
    }

    public AgentProperties getAgent() {
        return agent;
    }

    public void setAgent(AgentProperties agent) {
        this.agent = agent;
    }

    public class TeamProperties {
        private String ipPattern;
        private String internalIp;

        public String getIpPattern() {
            return ipPattern;
        }

        public void setIpPattern(String ipPattern) {
            this.ipPattern = ipPattern;
        }

        public String getInternalIp() {
            return internalIp;
        }

        public void setInternalIp(String internalIp) {
            this.internalIp = internalIp;
        }
    }

    public class FlagProperties {
        private int ttl;
        private Pattern pattern;

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }
    }

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

    public class AgentProperties {
        private Long timeout;
        private Long deleteAfter;

        public Long getTimeout() {
            return timeout;
        }

        public void setTimeout(Long timeout) {
            this.timeout = timeout;
        }

        public Long getDeleteAfter() {
            return deleteAfter;
        }

        public void setDeleteAfter(Long deleteAfter) {
            this.deleteAfter = deleteAfter;
        }
    }
}
