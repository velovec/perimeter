package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellAuthenticator;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ShellCommand(command = "passwd", description = "Change current user password")
public class PasswordCommand extends PerimeterShellCommand {

    private boolean isChanging = true;
    private PerimeterShellAuthenticator authManager;

    @Override
    protected void init() throws IOException {
        authManager = context.getBean(PerimeterShellAuthenticator.class);
    }

    public void execute() throws IOException {
        while (isChanging) {
            String newPassword = console.readPassword("Enter password: ");
            String passwordConfirmation = console.readPassword("Confirm password: ");

            if (newPassword.equals(passwordConfirmation)) {
                if (console.readYesNo("Save this password?")) {
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(newPassword.getBytes(StandardCharsets.UTF_8));
                        byte[] digest = md.digest();

                        authManager.setPassword(
                            getEnvironment().getEnv().get("USER"),
                            String.format("%064x", new java.math.BigInteger(1, digest))
                        );

                        console.writeLine("Password changed");
                    } catch (NoSuchAlgorithmException e) {
                        console.error("Internal error");
                        exit(1);
                        return;
                    }
                } else {
                    console.writeLine("Canceled");

                    exit(1);
                    return;
                }

                isChanging = false;
            } else {
                console.writeLine("Password mismatch");
            }
        }
    }

    @Override
    protected void onInterrupt() {
        isChanging = false;
    }
}