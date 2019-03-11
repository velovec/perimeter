package ru.v0rt3x.themis.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ru.v0rt3x.themis.server.team.Team;

import java.util.List;

@ConfigurationProperties(prefix = "themis")
public class ThemisProperties {

    private NetworkProperties network = new NetworkProperties();
    private JWTProperties jwt = new JWTProperties();
    private List<Team> teams;
    private Long duration;

    public NetworkProperties getNetwork() {
        return network;
    }

    public void setNetwork(NetworkProperties network) {
        this.network = network;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public JWTProperties getJwt() {
        return jwt;
    }

    public void setJwt(JWTProperties jwt) {
        this.jwt = jwt;
    }

    public class NetworkProperties {
        private String internal;
        private String team;
        private Integer teamSubnetCidr;

        public String getInternal() {
            return internal;
        }

        public void setInternal(String internal) {
            this.internal = internal;
        }

        public String getTeam() {
            return team;
        }

        public void setTeam(String team) {
            this.team = team;
        }

        public Integer getTeamSubnetCidr() {
            return teamSubnetCidr;
        }

        public void setTeamSubnetCidr(Integer teamSubnetCidr) {
            this.teamSubnetCidr = teamSubnetCidr;
        }
    }

    public class JWTProperties {
        private String algorithm;
        private String publicKey;
        private String privateKey;

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }
}
