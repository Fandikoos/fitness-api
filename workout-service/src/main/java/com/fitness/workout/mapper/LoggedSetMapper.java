package com.fitness.workout.mapper;

import com.fitness.workout.dto.LoggedSetRequest;
import com.fitness.workout.dto.LoggedSetResponse;
import com.fitness.workout.entity.LoggedSetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper de MapStruct para las series REALIZADAS (LoggedSetEntity).
 *
 * Es el equivalente de ExerciseSetMapper para el registro de entrenamientos.
 * Se mantiene separado aunque hoy los campos coincidan, porque mapea otra
 * entidad y otro DTO: si la serie realizada gana campos (calentamiento,
 * completada...), este mapper cambia sin tocar el de las series planificadas.
 */
@Mapper(componentModel = "spring")
public interface LoggedSetMapper {

    LoggedSetResponse toResponse(LoggedSetEntity entity);

    // id: lo genera JPA con @GeneratedValue al persistir.
    // loggedExercise: la relación con el padre se fija en el servicio,
    // igual que hace RoutineServiceImpl con set.setRoutineExercise(...).
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loggedExercise", ignore = true)
    LoggedSetEntity toEntity(LoggedSetRequest request);

    // MapStruct reutiliza toResponse() internamente para mapear cada elemento de la lista.
    List<LoggedSetResponse> toResponseList(List<LoggedSetEntity> entities);
}
