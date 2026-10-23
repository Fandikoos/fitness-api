package com.fitness.workout.util;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
