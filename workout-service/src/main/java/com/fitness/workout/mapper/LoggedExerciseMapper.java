package com.fitness.workout.mapper;

import com.fitness.workout.dto.LoggedExerciseResponse;
import com.fitness.workout.entity.LoggedExerciseEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper de MapStruct para los ejercicios registrados en una sesión.
 *
 * uses = {ExerciseTemplateMapper.class, LoggedSetMapper.class}: cuando MapStruct
 * tiene que convertir exerciseTemplate (ExerciseTemplateEntity → ExerciseTemplateResponse)
 * o sets (List<LoggedSetEntity> → List<LoggedSetResponse>), busca en esos mappers un
 * método con los tipos adecuados y lo llama, en vez de generar el código otra vez.
 *
 * No tiene toEntity(), igual que RoutineExerciseMapper: crear un LoggedExerciseEntity
 * exige buscar el ExerciseTemplateEntity en la BD a partir de exerciseTemplateId,
 * y eso es trabajo del servicio, no de un mapper (que no debe acceder a repositorios).
 */
@Mapper(componentModel = "spring", uses = {ExerciseTemplateMapper.class, LoggedSetMapper.class})
public interface LoggedExerciseMapper {

    LoggedExerciseResponse toResponse(LoggedExerciseEntity entity);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<LoggedExerciseResponse> toResponseList(List<LoggedExerciseEntity> entities);
}
