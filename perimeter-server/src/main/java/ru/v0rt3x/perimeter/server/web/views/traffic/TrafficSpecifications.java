package ru.v0rt3x.perimeter.server.web.views.traffic;

import org.springframework.data.jpa.domain.Specification;

import ru.v0rt3x.perimeter.server.web.views.traffic.tcp.TCPPacket;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

public class TrafficSpecifications {

    public static Specification<TCPPacket> isInbound(boolean inbound) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(
            root.<Boolean>get("inbound"), inbound
        );
    }

    public static Specification<TCPPacket> service(int service) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(
            root.<Integer>get("service"), service
        );
    }

    public static Specification<TCPPacket> transmission(int transmission) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(
            root.<Integer>get("transmission"), transmission
        );
    }

    public static Specification<TCPPacket> client(String client) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(
            root.<String>get("clientHost"), "%" + client + "%"
        );
    }

    public static Specification<TCPPacket> and(List<Specification<TCPPacket>> specifications) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(
            specifications.stream()
                .map(specification -> specification.toPredicate(root, criteriaQuery, criteriaBuilder))
                .collect(Collectors.toList())
                .toArray(new Predicate[] {})
        );
    }
}
