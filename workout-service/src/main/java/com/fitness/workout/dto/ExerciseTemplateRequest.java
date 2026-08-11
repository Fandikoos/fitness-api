package com.fitness.workout.dto;

import com.fitness.workout.enums.MuscleGroup;
import jakarta.validation.constraints.NotBlank;

public record ExerciseTemplateRequest(

        @NotBlank(message = "El nombre del ejercicio es obligatorio")
        String name,

        // Grupo muscular principal del ejercicio (ej. CHEST, BACK). Opcional.
        MuscleGroup muscleGroup,

        // URL de imagen opcional
        String imageUrl,

        // URL de vídeo demostrativo opcional
        String videoUrl
) {
}
