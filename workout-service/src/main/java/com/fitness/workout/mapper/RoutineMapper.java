package com.fitness.workout.mapper;

import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.entity.RoutineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para convertir entre RoutineEntity y sus DTOs.
 *
 * uses = {RoutineExerciseMapper.class} encadena los mappers: RoutineMapper usa
 * RoutineExerciseMapper, que a su vez usa ExerciseTemplateMapper y ExerciseSetMapper.
 * Así se convierte el árbol completo (Routine → RoutineExercises → Template + Sets).
 *
 * Nota: para toResponse, MapStruct mapea routineExercises → exercises automáticamente
 * gracias al @Mapping explícito. Para toEntity, ignoramos routineExercises porque
 * los ejercicios se gestionan por separado en el servicio (necesitan buscar templates en la BD).
 */
@Mapper(componentModel = "spring", uses = {RoutineExerciseMapper.class})
public interface RoutineMapper {

    // program.id se mapea a programId en el response.
    // routineExercises (nombre en la entidad) se mapea a exercises (nombre en el DTO).
    @Mapping(source = "program.id", target = "programId")
    @Mapping(source = "routineExercises", target = "exercises")
    RoutineResponse toResponse(RoutineEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    // program se establece manualmente en el servicio a partir del programId
    @Mapping(target = "program", ignore = true)
    // routineExercises se gestionan por separado en el servicio (necesitan buscar templates)
    @Mapping(target = "routineExercises", ignore = true)
    RoutineEntity toEntity(RoutineRequest request);
}
