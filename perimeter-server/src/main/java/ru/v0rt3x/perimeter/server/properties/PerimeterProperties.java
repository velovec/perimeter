package ru.v0rt3x.perimeter.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.v0rt3x.perimeter.server.utils.HexBin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "perimeter")
public class PerimeterProperties {

    private TeamProperties team = new TeamProperties();
    private FlagProperties flag = new FlagProperties();
    private ThemisProperties themis = new ThemisProperties();
    private AgentProperties agent = new AgentProperties();
    private ShellProperties shell = new ShellProperties();

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

    public ShellProperties getShell() {
        return shell;
    }

    public void setShell(ShellProperties shell) {
        this.shell = shell;
    }

    public class TeamProperties {
        private String internalIp;
        private Integer statsPort;
        private String productionBackend;
        private String baseNetwork;
        private int subnetCidr;
        private int vulnboxAddress;

        public String getInternalIp() {
            return internalIp;
        }

        public void setInternalIp(String internalIp) {
            this.internalIp = internalIp;
        }

        public Integer getStatsPort() {
            return statsPort;
        }

        public void setStatsPort(Integer statsPort) {
            this.statsPort = statsPort;
        }

        public String getProductionBackend() {
            return productionBackend;
        }

        public void setProductionBackend(String productionBackend) {
            this.productionBackend = productionBackend;
        }


        public String getBaseNetwork() {
            return baseNetwork;
        }

        public void setBaseNetwork(String baseNetwork) {
            this.baseNetwork = baseNetwork;
        }

        public int getSubnetCidr() {
            return subnetCidr;
        }

        public void setSubnetCidr(int subnetCidr) {
            this.subnetCidr = subnetCidr;
        }

        public int getVulnboxAddress() {
            return vulnboxAddress;
        }

        public void setVulnboxAddress(int vulnboxAddress) {
            this.vulnboxAddress = vulnboxAddress;
        }
    }

    public class FlagProperties {
        private int ttl;
        private Pattern pattern;
        private JwtProperties jwt = new JwtProperties();

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

        public JwtProperties getJwt() {
            return jwt;
        }

        public void setJwt(JwtProperties jwt) {
            this.jwt = jwt;
        }

        public class JwtProperties {
            private boolean enabled;
            private Pattern pattern;
            private String algorithm;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Pattern getPattern() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern = Pattern.compile(pattern);
            }

            public String getAlgorithm() {
                return algorithm;
            }

            public void setAlgorithm(String algorithm) {
                this.algorithm = algorithm;
            }
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

    public class ShellProperties {
        private String host;
        private Integer port;
        private String hostKey;
        private AuthStorageProperties authStorage = new AuthStorageProperties();

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

        public String getHostKey() {
            return hostKey;
        }

        public void setHostKey(String hostKey) {
            this.hostKey = hostKey;
        }

        public AuthStorageProperties getAuthStorage() {
            return authStorage;
        }

        public void setAuthStorage(AuthStorageProperties authStorage) {
            this.authStorage = authStorage;
        }

        public class AuthStorageProperties {
            private String path;
            private byte[] key;

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public byte[] getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = HexBin.decode(key);
            }
        }
    }
}
