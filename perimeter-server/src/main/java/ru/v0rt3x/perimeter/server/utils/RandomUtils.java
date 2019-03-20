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
}
