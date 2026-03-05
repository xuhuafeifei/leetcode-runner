package com.xhf.leetcode.plugin.utils;

import java.util.Random;
import java.util.UUID;

/**
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class RandomUtils {

    private static final Random RANDOM = new Random();

    /**
     * Generates a random integer within a specified range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer
     */
    public static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * Generates a random double within a specified range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random double
     */
    public static double nextDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        return min + (max - min) * RANDOM.nextDouble();
    }

    /**
     * Generates a random string of a specified length.
     *
     * @param length the length of the string
     * @return a random string
     */
    public static String nextString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) ('a' + RANDOM.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Generates a globally unique UUID.
     *
     * @return a UUID string
     */
    public static String nextUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a random boolean value.
     *
     * @return a random boolean
     */
    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }
}
