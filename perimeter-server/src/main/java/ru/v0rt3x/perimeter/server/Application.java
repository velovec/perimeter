package ru.v0rt3x.perimeter.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.v0rt3x.perimeter.server.properties.FlagProcessorProperties;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.properties.ThemisProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    PerimeterProperties.class,
    FlagProcessorProperties.class,
    ThemisProperties.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
