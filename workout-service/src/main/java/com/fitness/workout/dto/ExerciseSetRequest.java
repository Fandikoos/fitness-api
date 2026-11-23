package com.fitness.workout.dto;

import jakarta.validation.constraints.*;

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

        @NotNull(message = "El peso de es obligatorio")
        @Digits(integer = 3, fraction = 2, message = "El peso solo puede contener hasta 3 numeros enteros y 2 decimales")
        @DecimalMin(value = "0.0", message = "El peso no puede ser negativo")
        BigDecimal weightKg,

        @NotNull(message = "El número de repeticiones es obligatorio")
        @Min(value = 1, message = "El número mínimo de repeticiones tiene que ser 1")
        Integer reps,

        // RPE es opcional: no siempre se registra el esfuerzo percibido
        @Max(value = 10, message = "El RPE máximo es 10")
        @Min(value = 1, message = "El RPE mínimo es 1")
        Integer rpe
) {
}
