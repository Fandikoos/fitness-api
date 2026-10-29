package com.fitness.workout.dto;

import java.time.LocalDateTime;
import java.util.List;

// errors es null en errores genéricos (404, 500...) y contiene los mensajes
// por campo cuando falla la validación de Bean Validation (@NotBlank, @Min, etc.)
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<String> errors,
        LocalDateTime timestamp,
        String path
) {
}
