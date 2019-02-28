package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.files.FileRouter;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.storage.PerimeterStorageFile;
import ru.v0rt3x.perimeter.server.utils.RSAUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class PerimeterShellAuthenticator {

    @Autowired
    private PerimeterProperties properties;

    @Autowired
    private FileRouter fileRouter;

    private PerimeterStorageFile authStorage;

    private static final Logger logger = LoggerFactory.getLogger(PerimeterShellAuthenticator.class);

    @PostConstruct
    private void setUpAuthenticator() throws IOException {
        authStorage = new PerimeterStorageFile(
            Paths.get(properties.getShell().getAuthStorage().getPath()),
            properties.getShell().getAuthStorage().getKey()
        );

        fileRouter.addRoute(
            "public_key", Pattern.compile("^id_rsa\\.pub$"),
            (user, filenameMatcher, data) -> setUserKey(user, new String(data))
        );
    }

    @PreDestroy
    private void onShutdown() throws IOException {
        // authStorage.writeStorage();
        fileRouter.deleteRoute("public_key");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUser(String username) {
        try {
            if (!authStorage.hasKey(username)) {
                if (authStorage.listKeys().size() == 0) {
                    authStorage.putObject(username, new HashMap<String, Object>());
                } else {
                    return null;
                }
            }

            return Collections.unmodifiableMap(authStorage.getObject(username, Map.class));
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(String.format("Unable to read user data storage: %s", e));
        }
    }

    private void setUserData(String username, Map<String, Object> data) {
        try {
            authStorage.putObject(username, data);
            authStorage.writeStorage();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to write user data storage: %s", e));
        }
    }

    private String getHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] digest = md.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 in no available");
        }
    }

    public boolean authByPass(String username, String password, ServerSession serverSession) {
        Map<String, Object> userData = getUser(username);

        if (Objects.isNull(userData))
            return false;

        if (!userData.containsKey("password")) {
            userData = new HashMap<>(userData);
            userData.put("password", getHash(password));
            setUserData(username, userData);
        }

        return getHash(password).equals(userData.get("password"));
    }

    @SuppressWarnings("unchecked")
    public boolean authByKey(String username, PublicKey publicKey, ServerSession serverSession) {
        Map<String, Object> userData = getUser(username);

        if (Objects.isNull(userData))
            return false;

        if (!userData.containsKey("public_key"))
            return false;

        String publicKeyString = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        for (String publicKeyData: (List<String>) userData.get("public_key")) {
            if (publicKeyString.equals(publicKeyData)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean setUserKey(String username, String publicKey) {
        Map<String, Object> userData = getUser(username);

        if (Objects.isNull(userData))
            return false;

        userData = new HashMap<>(userData);

        try {
            KeySpec keySpec = RSAUtils.convertToRSAPublicKey(publicKey);
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
            String publicKeyString = new String(Base64.getEncoder().encode(pubKey.getEncoded()));

            List<String> publicKeyList = (userData.containsKey("public_key")) ? (List<String>) userData.get("public_key") : new ArrayList<>();

            publicKeyList.add(publicKeyString);

            userData.put("public_key", publicKeyList);
            setUserData(username, userData);

            return true;
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            logger.error("Unable to add SSH key for user {}", username);
            return false;
        }
    }

    public void createUser(String username, String password) {
        Map<String, Object> userData = new HashMap<>();

        userData.put("password", password);

        setUserData(username, userData);
    }

    public List<String> listUsers() {
        return new ArrayList<>(authStorage.listKeys());
    }

    public boolean userExists(String username) {
        return authStorage.hasKey(username);
    }

    public void deleteUser(String username) {
        authStorage.deleteKey(username);
    }

    public void setPassword(String username, String password) {
        Map<String, Object> userData = getUser(username);

        if (Objects.isNull(userData))
            return;

        userData = new HashMap<>(userData);

        userData.put("password", password);

        setUserData(username, userData);
    }

    public void setAttribute(String username, String key, Object value) {
        Map<String, Object> userData = getUser(username);

        if (Objects.isNull(userData))
            return;

        userData = new HashMap<>(userData);

        userData.put(key, value);

        setUserData(username, userData);
    }
}
