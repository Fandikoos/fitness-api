package com.fitness.workout.mapper;

import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para convertir entre ExerciseTemplateEntity y sus DTOs.
 *
 * Este mapper es sencillo porque ExerciseTemplateEntity no tiene relaciones
 * anidadas que convertir (a diferencia de RoutineExerciseMapper).
 */
@Mapper(componentModel = "spring")
public interface ExerciseTemplateMapper {

    ExerciseTemplateResponse toResponse(ExerciseTemplateEntity entity);

    @Mapping(target = "id", ignore = true)
    ExerciseTemplateEntity toEntity(ExerciseTemplateRequest request);
}
