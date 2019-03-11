package ru.v0rt3x.themis.server.flag.v1;

import java.util.Date;

public class FlagInfo {

    private String flag;
    private Date nbf;
    private Date exp;
    private Integer round;
    private String team;
    private String service;

    public FlagInfo(String flag, Date nbf, Date exp, Integer round, String team, String service) {
        this.flag = flag;
        this.nbf = nbf;
        this.exp = exp;
        this.round = round;
        this.team = team;
        this.service = service;
    }

    public String getFlag() {
        return flag;
    }

    public Date getNbf() {
        return nbf;
    }

    public Date getExp() {
        return exp;
    }

    public Integer getRound() {
        return round;
    }

    public String getTeam() {
        return team;
    }

    public String getService() {
        return service;
    }
}
