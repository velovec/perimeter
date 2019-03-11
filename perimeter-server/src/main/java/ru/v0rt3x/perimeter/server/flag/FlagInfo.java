package ru.v0rt3x.perimeter.server.flag;

import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.util.Date;
import java.util.Objects;

public class FlagInfo {

    private String flag;
    private Date nbf;
    private Date exp;
    private Integer round;
    private String team;
    private String service;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Date getNbf() {
        return nbf;
    }

    public void setNbf(Date nbf) {
        this.nbf = nbf;
    }

    public Date getExp() {
        return exp;
    }

    public void setExp(Date exp) {
        this.exp = exp;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Table toTable() {
        Table table = new Table("Flag", "Expires", "Round", "Service", "Team");
        table.addRow(flag, exp, round, service, team);

        return table;
    }

    @Override
    public String toString() {
        return String.format(
            "Flag(%s) : %s : (%d|%s|%s)",
            flag, exp, round, service, team
        );
    }

    public boolean isValid() {
        return Objects.nonNull(exp) && Objects.nonNull(round) &&
            Objects.nonNull(service) && Objects.nonNull(team);
    }

    public boolean isExpired() {
        return new Date().after(exp);
    }

    public static FlagInfo notFound(Flag flag) {
        FlagInfo flagInfo = new FlagInfo();

        flagInfo.setFlag(flag.getFlag());

        return flagInfo;
    }
}
