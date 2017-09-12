package ru.v0rt3x.perimeter.agent.types;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AgentInfo {

    private String hostName;

    private String osName;
    private String osArch;
    private String osVersion;

    private boolean isExecutor;
    private boolean isMonitor;

    public static AgentInfo build(AgentType type) {
        AgentInfo agentInfo = new AgentInfo();
        
        try {
            agentInfo.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            agentInfo.hostName = "unknown";
        }

        agentInfo.osName = System.getProperty("os.name");
        agentInfo.osArch = System.getProperty("os.arch");
        agentInfo.osVersion = System.getProperty("os.version");

        agentInfo.isExecutor = type == AgentType.EXECUTOR;
        agentInfo.isMonitor = type == AgentType.MONITOR;

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

    public boolean isExecutor() {
        return isExecutor;
    }

    public boolean isMonitor() {
        return isMonitor;
    }
}
