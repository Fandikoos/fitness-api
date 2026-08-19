package com.fitness.workout.mapper;

import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;
import com.fitness.workout.entity.ProgramEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper de MapStruct para convertir entre ProgramEntity y sus DTOs.
 *
 * uses = {RoutineMapper.class} encadena los mappers: al convertir un programa
 * a response, MapStruct usa RoutineMapper para convertir cada rutina hija,
 * que a su vez usa ExerciseMapper → ExerciseSetMapper. Así se convierte
 * el árbol completo automáticamente.
 */
@Mapper(componentModel = "spring", uses = {RoutineMapper.class})
public interface ProgramMapper {

    ProgramResponse toResponse(ProgramEntity entity);

    // Ignoramos id, createdAt y routines porque:
    // - id: lo genera la BD automáticamente (GenerationType.IDENTITY)
    // - createdAt: lo establece el @PrePersist de la entidad
    // - routines: las rutinas se gestionan por separado (no se crean inline con el programa)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "routines", ignore = true)
    ProgramEntity toEntity(ProgramRequest request);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<ProgramResponse> toResponseList(List<ProgramEntity> entities);
}
