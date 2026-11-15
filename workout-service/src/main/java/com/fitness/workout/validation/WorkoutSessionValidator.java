package com.fitness.workout.validation;

import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.exception.ConflictException;
import com.fitness.workout.repository.WorkoutSessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centraliza las validaciones de negocio del dominio WorkoutSession.
 * Sigue el mismo patrón que RoutineValidator: el servicio orquesta el flujo,
 * el validador garantiza las reglas de negocio (SRP).
 */
@Component
@AllArgsConstructor
public class WorkoutSessionValidator {

    private final WorkoutSessionRepository workoutSessionRepository;

    /**
     * Valida las reglas de negocio al crear una sesión.
     */
    public void validateForCreate(WorkoutSessionRequest request) {
        validateNoSessionForRoutineAndDate(request.routineId(), request.sessionDate());
    }

    /**
     * Garantiza que no exista ya una sesión para esa rutina en esa fecha.
     *
     * Lanza ConflictException (→ 409) porque el conflicto es con un registro
     * existente en la BD, no con los datos del request en sí.
     */
    private void validateNoSessionForRoutineAndDate(Long routineId, java.time.LocalDate sessionDate) {
        if (workoutSessionRepository.existsByRoutineIdAndSessionDate(routineId, sessionDate)) {
            throw new ConflictException(
                    "Ya existe una sesión de entrenamiento para la rutina con id: " + routineId +
                    " en la fecha: " + sessionDate
            );
        }
    }
}
