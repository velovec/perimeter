package ru.v0rt3x.perimeter.server.haproxy;

import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyACL;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyBackend;
import ru.v0rt3x.perimeter.server.service.dao.Service;

import java.util.List;
import java.util.Map;

public class HAProxyConfigWrapper {

    private List<Service> services;
    private List<HAProxyACL> acls;
    private List<HAProxyBackend> backends;
    private List<Map<String, String>> mappings;

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<HAProxyACL> getAcls() {
        return acls;
    }

    public void setAcls(List<HAProxyACL> acls) {
        this.acls = acls;
    }

    public List<HAProxyBackend> getBackends() {
        return backends;
    }

    public void setBackends(List<HAProxyBackend> backends) {
        this.backends = backends;
    }

    public List<Map<String, String>> getMappings() {
        return mappings;
    }

    public void setMappings(List<Map<String, String>> mappings) {
        this.mappings = mappings;
    }
}
