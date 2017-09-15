package ru.v0rt3x.perimeter.server.web.views.traffic.tcp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import ru.v0rt3x.perimeter.server.web.views.service.Service;

import java.util.List;

public interface TrafficRepository extends CrudRepository<TCPPacket, Long> {

    List<TCPPacket> findAll();

    Page<TCPPacket> findAll(Pageable pageable);

    TCPPacket findById(Integer id);

    List<TCPPacket> findAllByService(Service service);
}
