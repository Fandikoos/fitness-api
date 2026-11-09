package com.fitness.workout.mapper;

import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.dto.WorkoutSessionResponse;
import com.fitness.workout.entity.WorkoutSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper de MapStruct para las sesiones de entrenamiento.
 *
 * Encadena mappers igual que RoutineMapper:
 * WorkoutSessionMapper → LoggedExerciseMapper → ExerciseTemplateMapper + LoggedSetMapper.
 * Así toResponse() convierte el árbol completo (sesión → ejercicios → template + series).
 */
@Mapper(componentModel = "spring", uses = {LoggedExerciseMapper.class})
public interface WorkoutSessionMapper {

    // routine.id → routineId. MapStruct genera la comprobación de null por nosotros:
    // si la sesión no tiene rutina, routineId sale null en vez de lanzar NullPointerException.
    // Además, leer el id de un proxy LAZY no dispara ninguna consulta: Hibernate ya lo conoce
    // porque es la FK que tiene la propia fila de workout_sessions.
    @Mapping(source = "routine.id", target = "routineId")
    WorkoutSessionResponse toResponse(WorkoutSessionEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    // routine se busca en el servicio a partir de request.routineId()
    @Mapping(target = "routine", ignore = true)
    // routineName es la "foto" del nombre de la rutina el día del entreno:
    // el servicio lo copia de routine.getName(), no se confía en lo que mande el cliente
    @Mapping(target = "routineName", ignore = true)
    // exercises se construyen en el servicio: cada uno necesita buscar su
    // ExerciseTemplateEntity en la BD y enlazar la relación con la sesión.
    // Al ignorarlo, @Builder.Default deja la lista inicializada (vacía) en vez de null.
    @Mapping(target = "exercises", ignore = true)
    WorkoutSessionEntity toEntity(WorkoutSessionRequest request);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<WorkoutSessionResponse> toResponseList(List<WorkoutSessionEntity> entities);
}
