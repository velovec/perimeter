package ru.v0rt3x.themis.server.flag;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.v0rt3x.themis.server.properties.ThemisProperties;
import ru.v0rt3x.themis.server.utils.PEMUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

@RestController("/api/flag/generate")
public class FlagGeneratorController {

    @Autowired
    private ThemisProperties themisProperties;

    private Algorithm algorithm;

    @PostConstruct
    public void setUpJWT() {
        String privateKey = themisProperties.getJwt().getPrivateKey();

        try {
            switch (themisProperties.getJwt().getAlgorithm()) {
                case "EC256":
                    algorithm = Algorithm.ECDSA256(
                        null, (ECPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "EC")
                    );
                    break;
                case "EC384":
                    algorithm = Algorithm.ECDSA384(
                        null, (ECPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "EC")
                    );
                    break;
                case "EC512":
                    algorithm = Algorithm.ECDSA512(
                        null, (ECPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "EC")
                    );
                    break;
                case "RSA256":
                    algorithm = Algorithm.RSA256(
                        null, (RSAPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "RSA")
                    );
                    break;
                case "RSA384":
                    algorithm = Algorithm.RSA384(
                        null, (RSAPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "RSA")
                    );
                    break;
                case "RSA512":
                    algorithm = Algorithm.RSA512(
                        null, (RSAPrivateKey) PEMUtils.readPrivateKeyFromString(privateKey, "RSA")
                    );
                    break;
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to setup JWT: %s", e.getMessage()));
        }
    }

    private String generateFlag() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).array());

            return String.format("%s=", HexBin.encode(md.digest())).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    @RequestMapping(path = "/api/flag/generator", method = RequestMethod.GET)
    public String generate() {
        return JWT.create()
            .withClaim("flag", generateFlag())
            .sign(algorithm);
    }
}
