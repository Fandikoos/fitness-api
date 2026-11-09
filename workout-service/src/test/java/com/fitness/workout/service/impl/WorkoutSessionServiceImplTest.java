package com.fitness.workout.service.impl;

import com.fitness.workout.dto.*;
import com.fitness.workout.entity.*;
import com.fitness.workout.exception.ConflictException;
import com.fitness.workout.exception.EntityNotFoundException;
import com.fitness.workout.mapper.LoggedSetMapper;
import com.fitness.workout.mapper.WorkoutSessionMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.RoutineRepository;
import com.fitness.workout.repository.WorkoutSessionRepository;
import com.fitness.workout.validation.WorkoutSessionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para WorkoutSessionServiceImpl.
 *
 * @ExtendWith(MockitoExtension.class) integra Mockito con JUnit 5 sin levantar
 * el contexto de Spring. Cada dependencia del servicio se reemplaza por un mock
 * que podemos controlar con when/verify, aislando completamente la lógica del servicio.
 */
@ExtendWith(MockitoExtension.class)
class WorkoutSessionServiceImplTest {

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private WorkoutSessionMapper workoutSessionMapper;

    @Mock
    private ExerciseTemplateRepository exerciseTemplateRepository;

    @Mock
    private LoggedSetMapper loggedSetMapper;

    @Mock
    private WorkoutSessionValidator workoutSessionValidator;

    @InjectMocks
    private WorkoutSessionServiceImpl workoutSessionService;

    // --- createWorkoutSession ---

    @Test
    void should_createWorkoutSession_when_validRequest() {
        WorkoutSessionRequest request = buildRequest(1L, LocalDate.now());
        RoutineEntity routine = buildRoutine(1L, "Push Day");
        // Usamos el builder para que @Builder.Default inicialice la lista exercises
        WorkoutSessionEntity entity = buildSessionEntity(null);
        LoggedSetEntity setEntity = LoggedSetEntity.builder()
                .numberSet(1).weightKg(new BigDecimal("80.00")).reps(8).build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca");
        WorkoutSessionResponse expectedResponse = buildResponse(1L, "Push Day");

        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
        when(workoutSessionMapper.toEntity(request)).thenReturn(entity);
        when(exerciseTemplateRepository.findAllById(List.of(1L))).thenReturn(List.of(template));
        when(loggedSetMapper.toEntity(any(LoggedSetRequest.class))).thenReturn(setEntity);
        when(workoutSessionRepository.save(entity)).thenReturn(entity);
        when(workoutSessionMapper.toResponse(entity)).thenReturn(expectedResponse);

        WorkoutSessionResponse result = workoutSessionService.createWorkoutSession(request);

        assertThat(result).isEqualTo(expectedResponse);
        // Verificamos que el validador se invoocó antes de persistir
        verify(workoutSessionValidator).validateForCreate(request);
        verify(workoutSessionRepository).save(entity);
    }

    @Test
    void should_throwConflictException_when_sessionAlreadyExistsForRoutineAndDate() {
        WorkoutSessionRequest request = buildRequest(1L, LocalDate.now());

        // El validador lanza ConflictException (409) si ya existe sesión para esa rutina ese día
        doThrow(new ConflictException("Ya existe una sesión para esa rutina en esa fecha"))
                .when(workoutSessionValidator).validateForCreate(request);

        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(ConflictException.class);

        // Si el validador lanza, no debe llegar a consultar la BD ni a guardar nada
        verify(routineRepository, never()).findById(any());
        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void should_throwEntityNotFoundException_when_routineNotFoundOnCreate() {
        WorkoutSessionRequest request = buildRequest(99L, LocalDate.now());

        when(routineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void should_throwEntityNotFoundException_when_exerciseTemplateNotFound() {
        WorkoutSessionRequest request = buildRequest(1L, LocalDate.now());
        RoutineEntity routine = buildRoutine(1L, "Push Day");
        WorkoutSessionEntity entity = buildSessionEntity(null);

        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
        when(workoutSessionMapper.toEntity(request)).thenReturn(entity);
        // Lista vacía → el template con id 1L no existe en el catálogo
        when(exerciseTemplateRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("1");

        verify(workoutSessionRepository, never()).save(any());
    }

    // --- getBySessionDate ---

    @Test
    void should_returnSessionList_when_sessionsExistInDateRange() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();
        WorkoutSessionEntity entity = buildSessionEntity(1L);
        WorkoutSessionResponse response = buildResponse(1L, "Push Day");

        when(workoutSessionRepository.findBySessionDateBetweenOrderBySessionDateDesc(from, to))
                .thenReturn(List.of(entity));
        when(workoutSessionMapper.toResponseList(anyList())).thenReturn(List.of(response));

        List<WorkoutSessionResponse> result = workoutSessionService.getBySessionDate(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(response);
    }

    @Test
    void should_returnEmptyList_when_noSessionsInDateRange() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(workoutSessionRepository.findBySessionDateBetweenOrderBySessionDateDesc(from, to))
                .thenReturn(Collections.emptyList());

        List<WorkoutSessionResponse> result = workoutSessionService.getBySessionDate(from, to);

        assertThat(result).isEmpty();
        // Si no hay resultados, no tiene sentido llamar al mapper
        verify(workoutSessionMapper, never()).toResponseList(any());
    }

    // --- getById ---

    @Test
    void should_returnSession_when_idExists() {
        WorkoutSessionEntity entity = buildSessionEntity(1L);
        WorkoutSessionResponse expectedResponse = buildResponse(1L, "Push Day");

        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(workoutSessionMapper.toResponse(entity)).thenReturn(expectedResponse);

        WorkoutSessionResponse result = workoutSessionService.getById(1L);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void should_throwEntityNotFoundException_when_sessionNotFoundById() {
        when(workoutSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- getLatestWorkoutSessionByRoutineId ---

    @Test
    void should_returnLatestSession_when_sessionExistsForRoutine() {
        WorkoutSessionEntity entity = buildSessionEntity(1L);
        WorkoutSessionResponse expectedResponse = buildResponse(1L, "Push Day");

        when(workoutSessionRepository.findFirstByRoutineIdOrderBySessionDateDesc(1L))
                .thenReturn(Optional.of(entity));
        when(workoutSessionMapper.toResponse(entity)).thenReturn(expectedResponse);

        Optional<WorkoutSessionResponse> result = workoutSessionService.getLatestWorkoutSessionByRoutineId(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedResponse);
    }

    @Test
    void should_returnEmpty_when_noSessionExistsForRoutine() {
        when(workoutSessionRepository.findFirstByRoutineIdOrderBySessionDateDesc(99L))
                .thenReturn(Optional.empty());

        Optional<WorkoutSessionResponse> result = workoutSessionService.getLatestWorkoutSessionByRoutineId(99L);

        assertThat(result).isEmpty();
        // Optional.map no invoca al mapper si el Optional está vacío
        verify(workoutSessionMapper, never()).toResponse(any());
    }

    // --- delete ---

    @Test
    void should_deleteSession_when_idExists() {
        when(workoutSessionRepository.existsById(1L)).thenReturn(true);

        workoutSessionService.delete(1L);

        verify(workoutSessionRepository).deleteById(1L);
    }

    @Test
    void should_throwEntityNotFoundException_when_deletingNonExistentSession() {
        when(workoutSessionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutSessionService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(workoutSessionRepository, never()).deleteById(any());
    }

    // --- helpers ---

    private WorkoutSessionRequest buildRequest(Long routineId, LocalDate sessionDate) {
        LoggedSetRequest setRequest = new LoggedSetRequest(1, new BigDecimal("80.00"), 8, 7);
        LoggedExerciseRequest exerciseRequest = new LoggedExerciseRequest(1L, 1, null, List.of(setRequest));
        return new WorkoutSessionRequest(sessionDate, routineId, null, List.of(exerciseRequest));
    }

    private RoutineEntity buildRoutine(Long id, String name) {
        return RoutineEntity.builder()
                .id(id)
                .name(name)
                .build();
    }

    /**
     * Construye la entidad usando el builder para garantizar que @Builder.Default
     * inicialice la lista exercises como ArrayList vacío — el servicio llama a
     * getExercises().add(...) internamente y fallaría con null.
     */
    private WorkoutSessionEntity buildSessionEntity(Long id) {
        return WorkoutSessionEntity.builder()
                .id(id)
                .sessionDate(LocalDate.now())
                .routineName("Push Day")
                .build();
    }

    private ExerciseTemplateEntity buildTemplate(Long id, String name) {
        return ExerciseTemplateEntity.builder()
                .id(id)
                .name(name)
                .build();
    }

    private WorkoutSessionResponse buildResponse(Long id, String routineName) {
        return new WorkoutSessionResponse(id, LocalDate.now(), 1L, routineName, null,
                LocalDateTime.now(), Collections.emptyList());
    }
}
