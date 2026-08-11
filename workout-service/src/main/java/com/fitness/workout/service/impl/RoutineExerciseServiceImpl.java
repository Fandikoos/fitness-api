package com.fitness.workout.service.impl;

import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineExerciseResponse;
import com.fitness.workout.entity.ExerciseSetEntity;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.entity.RoutineExerciseEntity;
import com.fitness.workout.mapper.ExerciseSetMapper;
import com.fitness.workout.mapper.RoutineExerciseMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.RoutineExerciseRepository;
import com.fitness.workout.repository.RoutineRepository;
import com.fitness.workout.service.RoutineExerciseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Implementación del servicio de ejercicios en rutina.
 *
 * Gestiona la vinculación de ejercicios del catálogo a rutinas concretas,
 * incluyendo el orden, notas personalizadas y las series de cada ejercicio.
 */
@Service
@Transactional
public class RoutineExerciseServiceImpl implements RoutineExerciseService {

    private final RoutineExerciseRepository routineExerciseRepository;
    private final RoutineRepository routineRepository;
    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final RoutineExerciseMapper routineExerciseMapper;
    private final ExerciseSetMapper exerciseSetMapper;

    public RoutineExerciseServiceImpl(RoutineExerciseRepository routineExerciseRepository,
                                      RoutineRepository routineRepository,
                                      ExerciseTemplateRepository exerciseTemplateRepository,
                                      RoutineExerciseMapper routineExerciseMapper,
                                      ExerciseSetMapper exerciseSetMapper) {
        this.routineExerciseRepository = routineExerciseRepository;
        this.routineRepository = routineRepository;
        this.exerciseTemplateRepository = exerciseTemplateRepository;
        this.routineExerciseMapper = routineExerciseMapper;
        this.exerciseSetMapper = exerciseSetMapper;
    }

    @Override
    public RoutineExerciseResponse addToRoutine(Long routineId, RoutineExerciseRequest request) {
        RoutineEntity routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la rutina con id: " + routineId));

        ExerciseTemplateEntity template = exerciseTemplateRepository.findById(request.exerciseTemplateId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el ejercicio del catálogo con id: " + request.exerciseTemplateId()));

        // Construimos la entidad manualmente (no usamos mapper) porque necesitamos
        // buscar las entidades relacionadas (routine, template) en la BD por sus IDs.
        RoutineExerciseEntity routineExercise = RoutineExerciseEntity.builder()
                .exerciseTemplate(template)
                .routine(routine)
                .orderInRoutine(request.orderInRoutine())
                .notes(request.notes())
                .build();

        // Mapeamos las series y establecemos la relación bidireccional
        if (request.sets() != null) {
            List<ExerciseSetEntity> sets = request.sets().stream()
                    .map(exerciseSetMapper::toEntity)
                    .toList();
            sets.forEach(set -> set.setRoutineExercise(routineExercise));
            routineExercise.getSets().addAll(sets);
        }

        RoutineExerciseEntity saved = routineExerciseRepository.save(routineExercise);
        return routineExerciseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineExerciseResponse> findByRoutineId(Long routineId) {
        if (!routineRepository.existsById(routineId)) {
            throw new EntityNotFoundException(
                    "No se encontró la rutina con id: " + routineId);
        }
        return routineExerciseRepository.findByRoutineIdOrderByOrderInRoutineAsc(routineId).stream()
                .map(routineExerciseMapper::toResponse)
                .toList();
    }

    @Override
    public RoutineExerciseResponse update(Long routineId, Long routineExerciseId, RoutineExerciseRequest request) {
        RoutineExerciseEntity routineExercise = findRoutineExerciseInRoutine(routineId, routineExerciseId);

        // Si el template cambia, buscamos el nuevo
        if (!routineExercise.getExerciseTemplate().getId().equals(request.exerciseTemplateId())) {
            ExerciseTemplateEntity newTemplate = exerciseTemplateRepository.findById(request.exerciseTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No se encontró el ejercicio del catálogo con id: " + request.exerciseTemplateId()));
            routineExercise.setExerciseTemplate(newTemplate);
        }

        // Actualizamos campos simples
        routineExercise.setOrderInRoutine(request.orderInRoutine());
        routineExercise.setNotes(request.notes());

        // Reemplazamos las series: limpiamos y añadimos las nuevas.
        // orphanRemoval borra de la BD las series que ya no están en la lista.
        routineExercise.getSets().clear();

        List<ExerciseSetEntity> newSets = request.sets() != null
                ? request.sets().stream().map(exerciseSetMapper::toEntity).toList()
                : Collections.emptyList();

        newSets.forEach(set -> set.setRoutineExercise(routineExercise));
        routineExercise.getSets().addAll(newSets);

        return routineExerciseMapper.toResponse(routineExercise);
    }

    @Override
    public void delete(Long routineId, Long routineExerciseId) {
        RoutineExerciseEntity routineExercise = findRoutineExerciseInRoutine(routineId, routineExerciseId);
        routineExerciseRepository.delete(routineExercise);
    }

    /**
     * Busca un ejercicio en rutina verificando que pertenece a la rutina indicada.
     * Esto evita que alguien modifique un ejercicio de otra rutina pasando
     * un routineExerciseId válido pero con un routineId que no le corresponde.
     */
    private RoutineExerciseEntity findRoutineExerciseInRoutine(Long routineId, Long routineExerciseId) {
        RoutineExerciseEntity routineExercise = routineExerciseRepository.findById(routineExerciseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el ejercicio en rutina con id: " + routineExerciseId));

        if (!routineExercise.getRoutine().getId().equals(routineId)) {
            throw new EntityNotFoundException(
                    "El ejercicio " + routineExerciseId + " no pertenece a la rutina " + routineId);
        }

        return routineExercise;
    }
}
