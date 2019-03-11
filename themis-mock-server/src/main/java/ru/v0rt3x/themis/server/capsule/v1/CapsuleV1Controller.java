package ru.v0rt3x.themis.server.capsule.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.v0rt3x.themis.server.properties.ThemisProperties;

@RestController("/api/capsule/v1")
public class CapsuleV1Controller {

    @Autowired
    private ThemisProperties themisProperties;

    @RequestMapping(path = "/api/capsule/v1/public_key", method = RequestMethod.GET)
    public String getPublicKey() {
        return themisProperties.getJwt().getPublicKey();
    }
}
