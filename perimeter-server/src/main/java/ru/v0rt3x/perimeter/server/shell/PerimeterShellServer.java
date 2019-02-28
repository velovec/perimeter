package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.RejectAllForwardingFilter;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Paths;

@Component
public class PerimeterShellServer {

    private SshServer shellServer;

    @Autowired
    private PerimeterShellFactory shellFactory;

    @Autowired
    private PerimeterShellCommandFactory commandFactory;

    @Autowired
    private PerimeterShellAuthenticator authenticator;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @PostConstruct
    public void setUpShellServer() {
        shellServer = SshServer.setUpDefaultServer();

        shellServer.setHost(perimeterProperties.getShell().getHost());
        shellServer.setPort(perimeterProperties.getShell().getPort());

        shellServer.getProperties().put(SshServer.IDLE_TIMEOUT, 86400000L);

        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(
            Paths.get(perimeterProperties.getShell().getHostKey())
        );
        hostKeyProvider.setAlgorithm("RSA");

        shellServer.setKeyPairProvider(hostKeyProvider);

        shellServer.setPublickeyAuthenticator(authenticator::authByKey);
        shellServer.setPasswordAuthenticator(authenticator::authByPass);

        shellServer.setShellFactory(shellFactory);
        shellServer.setCommandFactory(commandFactory);

        shellServer.setForwardingFilter(RejectAllForwardingFilter.INSTANCE);
    }

    public void start() throws IOException {
        shellServer.start();
    }
}
