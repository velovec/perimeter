package ru.v0rt3x.perimeter.server.haproxy.dao;

import org.springframework.data.repository.CrudRepository;
import ru.v0rt3x.perimeter.server.service.dao.Service;

import java.util.List;

public interface HAProxyMappingRepository extends CrudRepository<HAProxyMapping, Long> {

    HAProxyMapping findByServiceAndAcl(Service service, HAProxyACL acl);

    List<HAProxyMapping> findAllByOrderByAclDesc();
}
