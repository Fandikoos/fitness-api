package com.fitness.workout.dto;

import java.math.BigDecimal;

public record LoggedSetResponse (
        Long id,
        Integer numberSet,
        BigDecimal weightKg,
        Integer reps,
        Integer rpe
) {
}
