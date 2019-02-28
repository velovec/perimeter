package ru.v0rt3x.perimeter.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellServer;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    PerimeterProperties.class
})
public class Application {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        ctx.registerShutdownHook();

        PerimeterShellServer shellServer = ctx.getBean(PerimeterShellServer.class);
        shellServer.start();
    }

}
