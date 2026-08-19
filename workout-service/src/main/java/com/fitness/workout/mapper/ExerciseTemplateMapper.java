package com.fitness.workout.mapper;

import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<ExerciseTemplateResponse> toResponseList(List<ExerciseTemplateEntity> entities);
}
