package ru.v0rt3x.perimeter.agent.types;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class AgentInfo {

    private String hostName;

    private String osName;
    private String osArch;
    private String osVersion;

    private String type;

    public static AgentInfo build(String type) {
        AgentInfo agentInfo = new AgentInfo();
        
        try {
            agentInfo.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            agentInfo.hostName = "unknown";
        }

        agentInfo.osName = System.getProperty("os.name");
        agentInfo.osArch = System.getProperty("os.arch");
        agentInfo.osVersion = System.getProperty("os.version");

        agentInfo.type = type;

        return agentInfo;
    }

    public String getHostName() {
        return hostName;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getType() {
        return type;
    }
}
