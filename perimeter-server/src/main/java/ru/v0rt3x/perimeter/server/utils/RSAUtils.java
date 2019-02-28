package ru.v0rt3x.perimeter.server.utils;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class RSAUtils {

    private static final String SSH_MARKER = "ssh-rsa";

    public static RSAPublicKeySpec convertToRSAPublicKey(String key) throws IOException {
        String[] parts = key.split("\\s");

        if ((parts.length < 2) || !"ssh-rsa".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid SSH key");
        }

        InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(parts[1]));

        String marker = new String(readLengthFirst(stream));
        if (!SSH_MARKER.equals(marker)) {
            throw new IllegalArgumentException("Invalid SSH key");
        }

        BigInteger publicExponent = new BigInteger(readLengthFirst(stream));
        BigInteger modulus = new BigInteger(readLengthFirst(stream));
        return new RSAPublicKeySpec(modulus, publicExponent);
    }

    private static byte[] readLengthFirst(InputStream in) throws IOException {
        int[] bytes = new int[]{ in.read(), in.read(), in.read(), in.read() };
        int length = 0;
        int shift = 24;

        for (int i = 0; i < bytes.length; i++) {
            length += bytes[i] << shift;
            shift -= 8;
        }

        byte[] val = new byte[length];

        ByteStreams.readFully(in, val);

        return val;
    }
}
