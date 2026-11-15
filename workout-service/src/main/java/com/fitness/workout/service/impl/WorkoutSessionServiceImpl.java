package com.fitness.workout.service.impl;

import com.fitness.workout.dto.LoggedExerciseRequest;
import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.dto.WorkoutSessionResponse;
import com.fitness.workout.entity.*;
import com.fitness.workout.exception.EntityNotFoundException;
import com.fitness.workout.mapper.LoggedSetMapper;
import com.fitness.workout.mapper.WorkoutSessionMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.RoutineRepository;
import com.fitness.workout.repository.WorkoutSessionRepository;
import com.fitness.workout.service.WorkoutSessionService;
import com.fitness.workout.util.ListUtils;
import com.fitness.workout.validation.WorkoutSessionValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class WorkoutSessionServiceImpl implements WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final RoutineRepository routineRepository;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final LoggedSetMapper loggedSetMapper;
    private final WorkoutSessionValidator workoutSessionValidator;


    @Override
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest workoutSessionRequest) {
        workoutSessionValidator.validateForCreate(workoutSessionRequest);

        RoutineEntity routine = routineRepository.findById(workoutSessionRequest.routineId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la rutina con id: " + workoutSessionRequest.routineId()));

        WorkoutSessionEntity workoutSessionEntity = workoutSessionMapper.toEntity(workoutSessionRequest);
        workoutSessionEntity.setRoutine(routine);
        workoutSessionEntity.setRoutineName(routine.getName());
        addExercisesToWorkoutSession(workoutSessionEntity, workoutSessionRequest.exercises());

        WorkoutSessionEntity savedWorkoutSession = workoutSessionRepository.save(workoutSessionEntity);
        return workoutSessionMapper.toResponse(savedWorkoutSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getBySessionDate(LocalDate sessionDateFrom, LocalDate sessionDateTo) {
        List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.findBySessionDateBetweenOrderBySessionDateDesc(sessionDateFrom, sessionDateTo);
        if (ListUtils.isNullOrEmpty(workoutSessions)) {
            return List.of();
        }
        return workoutSessionMapper.toResponseList(workoutSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutSessionResponse getById(Long id) {
       WorkoutSessionEntity workoutSessionEntity = workoutSessionRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("No se encontro la sesión de entrenamiento con id: " + id));
       return workoutSessionMapper.toResponse(workoutSessionEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getLatestWorkoutSessionByRoutineId(Long routineId) {
        // Optional.map transforma la entidad en response si existe, o propaga el vacío.
        // El controller mapea vacío → 204, presente → 200.
        return workoutSessionRepository.findFirstByRoutineIdOrderBySessionDateDesc(routineId)
                .map(workoutSessionMapper::toResponse);
    }

    @Override
    public void delete(Long id) {
        if (!workoutSessionRepository.existsById(id)) {
           throw new EntityNotFoundException("No se encontro la sesion de entrenamiento con id: " + id);
        }
        workoutSessionRepository.deleteById(id);
    }

    private void addExercisesToWorkoutSession(WorkoutSessionEntity workoutSessionEntity, List<LoggedExerciseRequest> exercises) {
        List<Long> idsExercisesTemplates = exercises.stream().map(LoggedExerciseRequest::exerciseTemplateId).toList();
        List<ExerciseTemplateEntity> exerciseTemplates = exerciseTemplateRepository.findAllById(idsExercisesTemplates);
        Map<Long, ExerciseTemplateEntity> exerciseTemplateEntityMap = exerciseTemplates.stream().collect(Collectors.toMap(
                ExerciseTemplateEntity::getId,
                template -> template
        ));

        for (LoggedExerciseRequest req : exercises) {
            ExerciseTemplateEntity template = exerciseTemplateEntityMap.get(req.exerciseTemplateId());

            if (template == null) {
                throw new EntityNotFoundException("No se encontró el ejercicio del catálogo con id: "
                        + req.exerciseTemplateId());
            }

            LoggedExerciseEntity loggedExercise = LoggedExerciseEntity.builder()
                    // Lado dueño de la relación (el que tiene @JoinColumn): sin esto,
                    // workout_session_id iría a NULL y fallaría el NOT NULL
                    .workoutSession(workoutSessionEntity)
                    .exerciseTemplate(template)
                    .orderInSession(req.orderInSession())
                    .notes(req.notes())
                    .build();

            List<LoggedSetEntity> sets = req.sets().stream()
                    .map(loggedSetMapper::toEntity)
                    .toList();
            sets.forEach(set -> set.setLoggedExercise(loggedExercise));
            loggedExercise.getSets().addAll(sets);

            // Lado inverso (mappedBy): es lo que hace que el cascade llegue a los hijos
            workoutSessionEntity.getExercises().add(loggedExercise);
        }

    }
}
