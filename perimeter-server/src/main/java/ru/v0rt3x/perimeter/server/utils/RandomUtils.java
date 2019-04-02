package ru.v0rt3x.perimeter.server.utils;

import java.util.List;
import java.util.Random;

public class RandomUtils {

    private static final Random random = new Random();

    @SafeVarargs
    public static <T> T oneOf(T... args) {
        return args[Math.abs(random.nextInt() % args.length)];
    }

    public static <T> T oneOf(List<T> args) {
        return args.get(Math.abs(random.nextInt() % args.size()));
    }

    public static boolean probabilityOf(double percentage) {
        return percentage < 100.0 - (random.nextDouble() * 100.0);
    }

    public static <T> T setWithProbability(double percentage, T positive, T negative) {
        return probabilityOf(percentage) ? positive : negative;
    }

    public static String randomHexString(int length) {
        byte[] randomBytes = new byte[(length % 2 == 0) ? length / 2 : (length + 1) / 2];

        random.nextBytes(randomBytes);

        return HexBin.encode(randomBytes).substring(0, length - 1);
    }
}
