package com.fitness.workout.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Serie REALIZADA (no planificada). Se separa de ExerciseSetRequest porque
 * representa lo que pasó, no un objetivo: peso y reps son obligatorios y
 * ambas pueden evolucionar por separado.
 */
public record LoggedSetRequest(

        @NotNull(message = "El número de serie es obligatorio")
        @Min(value = 1, message = "El número de serie debe ser al menos 1")
        Integer numberSet,

        // @Digits replica precision = 5, scale = 2 de la columna:
        // sin él, 1000 kg llegaría a la BD y saldría como 500 en vez de 400.
        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.0", message = "El peso no puede ser negativo")
        @Digits(integer = 3, fraction = 2, message = "Máximo 999.99 kg con 2 decimales")
        BigDecimal weightKg,

        // 0 se permite: un intento fallido también es información
        @NotNull(message = "Las repeticiones son obligatorias")
        @Min(value = 0, message = "Las repeticiones no pueden ser negativas")
        Integer reps,

        @Min(value = 1, message = "El RPE mínimo es 1")
        @Max(value = 10, message = "El RPE máximo es 10")
        Integer rpe
) {
}
