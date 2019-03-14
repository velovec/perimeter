package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.ForwardingFilter;
import org.springframework.stereotype.Component;

@Component
public class PerimeterShellForwardingFilter implements ForwardingFilter {

    @Override
    public boolean canForwardAgent(Session session, String s) {
        return false; // Agent forwarding is not supported
    }

    @Override
    public boolean canForwardX11(Session session, String s) {
        return false; // X11 forwarding is not supported
    }

    @Override
    public boolean canListen(SshdSocketAddress sshdSocketAddress, Session session) {
        return false;
    }

    @Override
    public boolean canConnect(Type type, SshdSocketAddress sshdSocketAddress, Session session) {
        return false;
    }
}
