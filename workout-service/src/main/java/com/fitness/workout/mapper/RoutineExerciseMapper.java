package com.fitness.workout.mapper;

import com.fitness.workout.dto.RoutineExerciseResponse;
import com.fitness.workout.entity.RoutineExerciseEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper de MapStruct para convertir RoutineExerciseEntity a su DTO de respuesta.
 *
 * uses = {ExerciseTemplateMapper.class, ExerciseSetMapper.class} indica que este mapper
 * delega la conversión del template y de las series a sus mappers respectivos.
 * MapStruct los inyecta automáticamente y los usa cuando encuentra tipos que coinciden.
 *
 * No tiene toEntity() porque la creación de RoutineExerciseEntity se hace manualmente
 * en el servicio, ya que necesita buscar el ExerciseTemplateEntity por su ID en la BD.
 */
@Mapper(componentModel = "spring", uses = {ExerciseTemplateMapper.class, ExerciseSetMapper.class})
public interface RoutineExerciseMapper {

    RoutineExerciseResponse toResponse(RoutineExerciseEntity entity);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<RoutineExerciseResponse> toResponseList(List<RoutineExerciseEntity> entities);
}
