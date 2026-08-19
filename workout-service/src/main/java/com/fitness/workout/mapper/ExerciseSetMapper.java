package com.fitness.workout.mapper;

import com.fitness.workout.dto.ExerciseSetRequest;
import com.fitness.workout.dto.ExerciseSetResponse;
import com.fitness.workout.entity.ExerciseSetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper de MapStruct para convertir entre ExerciseSetEntity y sus DTOs.
 *
 * componentModel = "spring" hace que MapStruct genere una implementación
 * anotada con @Component, permitiendo inyectarla como cualquier bean de Spring.
 * MapStruct genera el código de mapeo en tiempo de compilación (no reflexión),
 * lo que lo hace más rápido y seguro que alternativas basadas en reflexión.
 */
@Mapper(componentModel = "spring")
public interface ExerciseSetMapper {

    ExerciseSetResponse toResponse(ExerciseSetEntity entity);

    // @Mapping(target = "id", ignore = true): ignoramos el id porque al crear
    // una entidad nueva, JPA lo genera automáticamente con @GeneratedValue.
    // Lo mismo con "routineExercise": la relación padre se establece en el servicio.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "routineExercise", ignore = true)
    ExerciseSetEntity toEntity(ExerciseSetRequest request);

    // Método para actualizar una entidad existente con datos del request,
    // sin crear una nueva instancia. Útil para operaciones de update.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "routineExercise", ignore = true)
    void updateEntity(ExerciseSetRequest request, @MappingTarget ExerciseSetEntity entity);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<ExerciseSetResponse> toResponseList(List<ExerciseSetEntity> entities);
}
