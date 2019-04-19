package ru.v0rt3x.perimeter.judas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.v0rt3x.perimeter.agent.properties.PerimeterAgentProperties;
import ru.v0rt3x.perimeter.judas.properties.PerimeterJudasProperties;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    PerimeterAgentProperties.class,
    PerimeterJudasProperties.class
})
@ComponentScan({"ru.v0rt3x.perimeter.agent", "ru.v0rt3x.perimeter.judas"})
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        ctx.registerShutdownHook();
    }

}
