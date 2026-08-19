package com.fitness.workout.service.impl;

import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.entity.ExerciseSetEntity;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.entity.ProgramEntity;
import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.entity.RoutineExerciseEntity;
import com.fitness.workout.enums.DayOfWeek;
import com.fitness.workout.mapper.ExerciseSetMapper;
import com.fitness.workout.mapper.RoutineMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.ProgramRepository;
import com.fitness.workout.repository.RoutineRepository;
import com.fitness.workout.service.RoutineService;
import com.fitness.workout.util.ListUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación del servicio de rutinas.
 *
 * @Service marca esta clase como un bean de la capa de servicio.
 * Spring lo detecta automáticamente y lo registra en el contexto de la app.
 *
 * @Transactional a nivel de clase hace que todos los métodos públicos
 * se ejecuten dentro de una transacción de BD. Si algo falla, se hace rollback
 * automático y la BD queda en un estado consistente.
 */
@Service
@Transactional
public class RoutineServiceImpl implements RoutineService {

    private final RoutineRepository routineRepository;
    private final ProgramRepository programRepository;
    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final RoutineMapper routineMapper;
    private final ExerciseSetMapper exerciseSetMapper;

    public RoutineServiceImpl(RoutineRepository routineRepository,
                              ProgramRepository programRepository,
                              ExerciseTemplateRepository exerciseTemplateRepository,
                              RoutineMapper routineMapper,
                              ExerciseSetMapper exerciseSetMapper) {
        this.routineRepository = routineRepository;
        this.programRepository = programRepository;
        this.exerciseTemplateRepository = exerciseTemplateRepository;
        this.routineMapper = routineMapper;
        this.exerciseSetMapper = exerciseSetMapper;
    }

    @Override
    public RoutineResponse create(RoutineRequest request) {
        RoutineEntity routine = routineMapper.toEntity(request);

        // Si se envía un programId, buscamos el programa y lo asignamos a la rutina
        assignProgram(routine, request.programId());

        // Si se envían ejercicios en la creación, los vinculamos
        addExercisesToRoutine(routine, request.exercises());

        RoutineEntity saved = routineRepository.save(routine);
        return routineMapper.toResponse(saved);
    }

    // readOnly = true optimiza la transacción: Hibernate no necesita
    // comprobar cambios (dirty checking) al final, mejorando el rendimiento.
    @Override
    @Transactional(readOnly = true)
    public RoutineResponse findById(Long id) {
        RoutineEntity routine = routineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la rutina con id: " + id));
        return routineMapper.toResponse(routine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> findAll() {
        return routineRepository.findAll().stream()
                .map(routineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return routineRepository.findByDayOfWeek(dayOfWeek).stream()
                .map(routineMapper::toResponse)
                .toList();
    }

    @Override
    public RoutineResponse update(Long id, RoutineRequest request) {
        RoutineEntity routine = routineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la rutina con id: " + id));

        // Actualizamos los campos simples de la rutina
        routine.setName(request.name());
        routine.setDayOfWeek(request.dayOfWeek());
        routine.setDescription(request.description());

        // Actualizamos la relación con el programa (puede cambiar o eliminarse)
        assignProgram(routine, request.programId());

        // Para actualizar los ejercicios, limpiamos la lista actual y añadimos los nuevos.
        // orphanRemoval = true se encarga de borrar de la BD los que ya no están.
        routine.getRoutineExercises().clear();
        addExercisesToRoutine(routine, request.exercises());

        // No necesitamos llamar a save() explícitamente: como la entidad está
        // "managed" (cargada dentro de la transacción), JPA detecta los cambios
        // automáticamente al final de la transacción (dirty checking).
        return routineMapper.toResponse(routine);
    }

    @Override
    public void delete(Long id) {
        if (!routineRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se encontró la rutina con id: " + id);
        }
        // deleteById borra la rutina y, gracias a cascade ALL,
        // también todos sus ejercicios vinculados y sus series.
        routineRepository.deleteById(id);
    }

    @Override
    public List<RoutineResponse> findByProgramId(Long programId) {
        List<RoutineEntity> routineEntities = routineRepository.findByProgramId(programId);
        if (ListUtils.isNullOrEmpty(routineEntities)) {
            return new ArrayList<>();
        }
        return routineMapper.toResponseList(routineEntities);
    }

    /**
     * Asigna un programa a la rutina a partir de su ID.
     * Si programId es null, desvincula la rutina de cualquier programa.
     * Si el programId no existe en la BD, lanza EntityNotFoundException.
     */
    private void assignProgram(RoutineEntity routine, Long programId) {
        if (programId != null) {
            ProgramEntity program = programRepository.findById(programId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No se encontró el programa con id: " + programId));
            routine.setProgram(program);
        } else {
            routine.setProgram(null);
        }
    }

    /**
     * Convierte los RoutineExerciseRequest a entidades y los vincula a la rutina.
     * Para cada ejercicio, busca el template correspondiente en la BD y establece
     * las relaciones bidireccionales (routine ↔ routineExercise ↔ sets).
     */
    private void addExercisesToRoutine(RoutineEntity routine, List<RoutineExerciseRequest> exerciseRequests) {
        if (ListUtils.isNullOrEmpty(exerciseRequests)) {
            return;
        }

        for (RoutineExerciseRequest req : exerciseRequests) {
            ExerciseTemplateEntity template = exerciseTemplateRepository.findById(req.exerciseTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No se encontró el ejercicio del catálogo con id: " + req.exerciseTemplateId()));

            RoutineExerciseEntity routineExercise = RoutineExerciseEntity.builder()
                    .exerciseTemplate(template)
                    .routine(routine)
                    .orderInRoutine(req.orderInRoutine())
                    .notes(req.notes())
                    .build();

            // Mapeamos las series y establecemos la relación bidireccional
            if (req.sets() != null) {
                List<ExerciseSetEntity> sets = req.sets().stream()
                        .map(exerciseSetMapper::toEntity)
                        .toList();
                sets.forEach(set -> set.setRoutineExercise(routineExercise));
                routineExercise.getSets().addAll(sets);
            }

            routine.getRoutineExercises().add(routineExercise);
        }
    }
}
