package ru.v0rt3x.perimeter.server.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.web.types.Service;
import ru.v0rt3x.perimeter.server.web.types.Team;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ThemisClient themisClient;

    @RequestMapping(method = RequestMethod.GET)
    public List<Service> getTeams() {
        return themisClient.getServiceList();
    }
}
