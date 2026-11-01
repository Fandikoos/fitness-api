package com.fitness.workout.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada para una serie de ejercicio.
 *
 * Usamos record en lugar de clase porque un DTO es solo un contenedor
 * inmutable de datos — record genera automáticamente constructor, getters,
 * equals, hashCode y toString.
 */
public record ExerciseSetRequest(

        @NotNull(message = "El número de serie es obligatorio")
        @Min(value = 1, message = "El número de serie debe ser al menos 1")
        Integer numberSet,

        @DecimalMin(value = "0.0", message = "El peso no puede ser negativo")
        BigDecimal weightKg,

        Integer reps,

        // RPE es opcional: no siempre se registra el esfuerzo percibido
        @Max(value = 10, message = "El RPE máximo es 10")
        Integer rpe
) {
}
