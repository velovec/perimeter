package ru.v0rt3x.perimeter.server.web.views.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@UIView(name = "service", linkOrder = 6, link = "/service/", title = "Services", icon = "dns")
public class ServiceView extends UIBaseView {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ThemisClient themisClient;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @ModelAttribute("SERVICES")
    public List<Service> getServices() {
        return serviceRepository.findAll();
    }

    @MessageMapping("/service/request_sync")
    public void syncRequest() {
        notify("sync_service", themisClient.getServiceList());
    }

    @MessageMapping("/service/confirm_sync")
    public void syncConfirm(Service[] services) {
        List<String> invalidServices = Arrays.stream(services)
            .filter(service -> service.getPort() < 1 || service.getPort() > 65535)
            .map(Service::getName)
            .collect(Collectors.toList());

        if (invalidServices.size() > 0) {
            emitter.sendNotificationEvent("danger", String.format("Invalid port for services: %s", invalidServices));
            return;
        }

        List<Service> existingServices = Arrays.stream(services)
            .map(service -> serviceRepository.findByName(service.getName()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        deleteAndNotify(serviceRepository, existingServices);
        saveAndNotify(serviceRepository, Arrays.asList(services));
    }

    @RequestMapping(value = "/service/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        addNavButton(context,"add", "success", "data-toggle=\"modal\" data-target=\"#addService\"");
        addNavButton(context,"sync", "primary", "data-toggle=\"modal\" data-target=\"#syncServices\" onclick=\"requestServiceSync();\"");

        return "service";
    }

    @Scheduled(fixedRate = 5000L)
    private void checkServicesStatus() {
        serviceRepository.findAll().parallelStream().forEach(
            service -> {
                boolean isRunning = isServiceRunning(service);
                if (service.isAvailable() != isRunning) {
                    service.setAvailable(isRunning);
                    saveAndNotify(serviceRepository, service);
                }
            }
        );
    }

    private boolean isServiceRunning(Service service) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(perimeterProperties.getTeamIp(), service.getPort()), 5);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
