package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellAuthenticator;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ShellCommand(command = "users", description = "Manage users settings")
public class UserProfileCommand extends PerimeterShellCommand {

    private PerimeterShellAuthenticator authManager;

    @Override
    protected void init() throws IOException {
        authManager = context.getBean(PerimeterShellAuthenticator.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List users")
    public void list() throws IOException {
        Table users = new Table(
            authManager.listUsers(),
            "Users"
        );
        console.write(users);
    }

    @CommandAction("Show user profile")
    public void show() throws IOException {
        String userName = (args.size() > 0) ? args.get(0) : getEnv("USER");

        Map<String, Object> authUser = authManager.getUser(userName);
        if (Objects.isNull(authUser)) {
            console.error("User '{}' not found", userName);
            exit(1);
            return;
        }

        Map<String, String> userAttributes = new HashMap<>();

        authUser.keySet().stream()
            .filter(attribute -> !attribute.equals("public_key"))
            .filter(attribute -> !attribute.equals("password"))
            .forEach(attribute -> userAttributes.put(attribute, String.valueOf(authUser.get(attribute))));

        console.write(new Table(userAttributes, "Attribute", "Value"));
    }

    @CommandAction("Add user")
    public void add() throws IOException, NoSuchAlgorithmException {
        String userName = (args.size() > 0) ? args.get(0) : null;

        if (userName != null) {
            Map<String, Object> user = authManager.getUser(userName);

            if (user != null) {
                console.writeLine("User %s already exists!", userName);
                exit(1);
            } else {
                boolean passwordSet = false;
                while (!passwordSet) {
                    String password = console.readPassword("Enter password: ");
                    String passwordConfirmation = console.readPassword("Confirm password: ");

                    if (password.equals(passwordConfirmation)) {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(password.getBytes(StandardCharsets.UTF_8));
                        byte[] digest = md.digest();

                        authManager.createUser(userName, String.format("%064x", new java.math.BigInteger(1, digest)));

                        console.writeLine("User created");

                        passwordSet = true;
                    } else {
                        console.writeLine("Password mismatch");
                    }
                }
            }
        } else {
            console.writeLine("Username not specified");
            exit(1);
        }
    }

    @CommandAction("Delete user")
    public void delete() throws IOException {
        String userName = (args.size() > 0) ? args.get(0) : null;

        if (Objects.nonNull(userName)) {
            if (userName.equals(getEnv("USER"))) {
                console.writeLine("Unable to delete yourself");
                exit(1);
                return;
            }

            if (authManager.userExists(userName)) {
                if (console.readYesNo("Do you really want to delete this user?")) {
                    authManager.deleteUser(userName);
                }
            } else {
                console.writeLine("User %s doesn't exists!", userName);
                exit(1);
            }
        } else {
            console.writeLine("Username not specified");
            exit(1);
        }
    }

    @CommandAction("Set SSH public key")
    public void set_key() throws IOException {
        authManager.setUserKey(getEnv("USER"), console.readLine());
    }

    @CommandAction("Set user attribute")
    public void set() throws IOException {
        if (args.size() < 2) {
            console.error("users set <key> <value>");
            exit(1);
            return;
        }

        authManager.setAttribute(getEnv("USER"), args.get(0), String.join(" ", args.subList(1, args.size() - 1)));
    }

    @Override
    protected void onInterrupt() {

    }
}