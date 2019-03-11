package ru.v0rt3x.themis.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.v0rt3x.themis.server.properties.ThemisProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    ThemisProperties.class
})
public class Application {

    public static void main(String[] args)  {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        ctx.registerShutdownHook();
    }

}
