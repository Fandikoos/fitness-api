package com.fitness.workout.dto;

import jakarta.validation.constraints.NotBlank;

public record ProgramRequest(

        @NotBlank(message = "El nombre del programa es obligatorio")
        String name,

        // Descripción opcional del programa
        String description,

        // Si el programa está activo o no. Nullable: si no se envía, por defecto será false.
        Boolean active
) {
}
