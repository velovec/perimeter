package ru.v0rt3x.perimeter.server.vulnbox;

import com.jcraft.jsch.UserInfo;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleUtils;

import java.io.IOException;

public class VulnBoxUserInfo implements UserInfo {

    private ConsoleUtils console;

    public VulnBoxUserInfo(ConsoleUtils consoleUtils) {
        this.console = consoleUtils;
    }

    @Override
    public String getPassphrase() {
        try {
            return console.readPassword("Enter passphrase: ");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getPassword() {
        try {
            return console.readPassword("Enter password: ");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean promptPassword(String s) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String s) {
        return true;
    }

    @Override
    public boolean promptYesNo(String s) {
        try {
            return console.readYesNo(s);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void showMessage(String s) {
        try {
            console.writeLine(s);
        } catch (IOException e) {}
    }
}
