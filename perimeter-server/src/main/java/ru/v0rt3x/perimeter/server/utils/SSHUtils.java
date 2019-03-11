package ru.v0rt3x.perimeter.server.utils;

import com.jcraft.jsch.*;

import java.util.Objects;

public class SSHUtils {

    private static final JSch jSch = new JSch();

    public static Session getSession(String username, String host, int port, String password, UserInfo userInfo) throws JSchException {
        Session session = jSch.getSession(username, host, port);

        if (Objects.nonNull(password)) {
            session.setPassword(password);
        }

        session.setUserInfo(userInfo);

        session.connect();

        return session;
    }

}
