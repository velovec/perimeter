package ru.v0rt3x.perimeter.judas;

public class JudasInstance {

    private Integer port;
    private String target;

    public JudasInstance(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
