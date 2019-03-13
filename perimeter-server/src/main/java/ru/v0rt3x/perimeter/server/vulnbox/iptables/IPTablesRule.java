package ru.v0rt3x.perimeter.server.vulnbox.iptables;

public class IPTablesRule {

    private final String target;
    private final String protocol;
    private final String options;
    private final String source;
    private final String destination;
    private final String extra;

    public IPTablesRule(String target, String prot, String opt, String source, String destination, String extra) {
        this.target = target;
        this.protocol = prot;
        this.options = opt;
        this.source = source;
        this.destination = destination;
        this.extra = extra;
    }

    public String getTarget() {
        return target;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getOptions() {
        return options;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getExtra() {
        return extra;
    }
}
