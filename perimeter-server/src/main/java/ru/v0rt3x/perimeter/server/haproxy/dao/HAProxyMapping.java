package ru.v0rt3x.perimeter.server.haproxy.dao;

import ru.v0rt3x.perimeter.server.service.dao.Service;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class HAProxyMapping {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private Service service;

    @ManyToOne
    private HAProxyBackend backend;

    @ManyToOne
    private HAProxyACL acl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public HAProxyBackend getBackend() {
        return backend;
    }

    public void setBackend(HAProxyBackend backend) {
        this.backend = backend;
    }

    public HAProxyACL getAcl() {
        return acl;
    }

    public void setAcl(HAProxyACL acl) {
        this.acl = acl;
    }
}
