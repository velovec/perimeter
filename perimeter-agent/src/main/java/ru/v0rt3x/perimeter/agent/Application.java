package ru.v0rt3x.perimeter.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.v0rt3x.perimeter.agent.properties.PerimeterProperties;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    PerimeterProperties.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
