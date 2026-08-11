package com.fitness.workout.util;

import java.util.Collection;

public final class ListUtils {

    private ListUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
