package com.fitness.workout.service.impl;

import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineExerciseResponse;
import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.entity.RoutineExerciseEntity;
import com.fitness.workout.enums.DayOfWeek;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.mapper.ExerciseSetMapper;
import com.fitness.workout.mapper.RoutineMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.ProgramRepository;
import com.fitness.workout.repository.RoutineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RoutineServiceImpl.
 *
 * @ExtendWith(MockitoExtension.class) integra Mockito con JUnit 5,
 * habilitando las anotaciones @Mock e @InjectMocks.
 * No levanta el contexto de Spring — son tests de unidad puros y rápidos.
 */
@ExtendWith(MockitoExtension.class)
class RoutineServiceImplTest {

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ExerciseTemplateRepository exerciseTemplateRepository;

    @Mock
    private RoutineMapper routineMapper;

    @Mock
    private ExerciseSetMapper exerciseSetMapper;

    @InjectMocks
    private RoutineServiceImpl routineService;

    // --- create ---

    @Test
    void should_createRoutine_when_validRequest() {
        RoutineRequest request = new RoutineRequest("Push Day", DayOfWeek.MONDAY, "Pecho y tríceps", null, null);
        RoutineEntity entity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineResponse expectedResponse = buildRoutineResponse(1L, "Push Day", DayOfWeek.MONDAY);

        when(routineMapper.toEntity(request)).thenReturn(entity);
        when(routineRepository.save(entity)).thenReturn(entity);
        when(routineMapper.toResponse(entity)).thenReturn(expectedResponse);

        RoutineResponse result = routineService.create(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(routineRepository).save(entity);
    }

    @Test
    void should_createRoutineWithExercises_when_requestHasExercises() {
        RoutineExerciseRequest exerciseReq = new RoutineExerciseRequest(1L, 1, null, null);
        RoutineRequest request = new RoutineRequest("Push Day", DayOfWeek.MONDAY, null, null, List.of(exerciseReq));
        RoutineEntity entity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineResponse expectedResponse = buildRoutineResponse(1L, "Push Day", DayOfWeek.MONDAY);

        when(routineMapper.toEntity(request)).thenReturn(entity);
        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(routineRepository.save(entity)).thenReturn(entity);
        when(routineMapper.toResponse(entity)).thenReturn(expectedResponse);

        RoutineResponse result = routineService.create(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(exerciseTemplateRepository).findById(1L);
        verify(routineRepository).save(entity);
    }

    // --- findById ---

    @Test
    void should_returnRoutine_when_idExists() {
        RoutineEntity entity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineResponse expectedResponse = buildRoutineResponse(1L, "Push Day", DayOfWeek.MONDAY);

        when(routineRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(routineMapper.toResponse(entity)).thenReturn(expectedResponse);

        RoutineResponse result = routineService.findById(1L);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.name()).isEqualTo("Push Day");
    }

    @Test
    void should_throwException_when_routineNotFound() {
        when(routineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- findAll ---

    @Test
    void should_returnAllRoutines_when_routinesExist() {
        RoutineEntity entity1 = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineEntity entity2 = buildRoutineEntity(2L, "Pull Day", DayOfWeek.WEDNESDAY);
        RoutineResponse response1 = buildRoutineResponse(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineResponse response2 = buildRoutineResponse(2L, "Pull Day", DayOfWeek.WEDNESDAY);

        when(routineRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(routineMapper.toResponse(entity1)).thenReturn(response1);
        when(routineMapper.toResponse(entity2)).thenReturn(response2);

        List<RoutineResponse> result = routineService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void should_returnEmptyList_when_noRoutinesExist() {
        when(routineRepository.findAll()).thenReturn(Collections.emptyList());

        List<RoutineResponse> result = routineService.findAll();

        assertThat(result).isEmpty();
    }

    // --- findByDayOfWeek ---

    @Test
    void should_returnRoutines_when_filterByDayOfWeek() {
        RoutineEntity entity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineResponse expectedResponse = buildRoutineResponse(1L, "Push Day", DayOfWeek.MONDAY);

        when(routineRepository.findByDayOfWeek(DayOfWeek.MONDAY)).thenReturn(List.of(entity));
        when(routineMapper.toResponse(entity)).thenReturn(expectedResponse);

        List<RoutineResponse> result = routineService.findByDayOfWeek(DayOfWeek.MONDAY);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    // --- update ---

    @Test
    void should_updateRoutine_when_idExists() {
        RoutineEntity existingEntity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        RoutineRequest updateRequest = new RoutineRequest("Push Day V2", DayOfWeek.TUESDAY, "Actualizado", null, null);
        RoutineResponse expectedResponse = new RoutineResponse(1L, "Push Day V2", DayOfWeek.TUESDAY, "Actualizado",
                null, LocalDateTime.now(), Collections.emptyList());

        when(routineRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(routineMapper.toResponse(existingEntity)).thenReturn(expectedResponse);

        RoutineResponse result = routineService.update(1L, updateRequest);

        assertThat(result.name()).isEqualTo("Push Day V2");
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        verify(routineRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_updatingNonExistentRoutine() {
        RoutineRequest request = new RoutineRequest("Push Day", DayOfWeek.MONDAY, null, null, null);

        when(routineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void should_replaceExercises_when_updatingWithNewExercises() {
        RoutineEntity existingEntity = buildRoutineEntity(1L, "Push Day", DayOfWeek.MONDAY);
        // Añadimos un ejercicio existente para verificar que se limpia la lista
        RoutineExerciseEntity oldExercise = RoutineExerciseEntity.builder().id(10L).orderInRoutine(1).build();
        existingEntity.getRoutineExercises().add(oldExercise);

        RoutineExerciseRequest newExerciseReq = new RoutineExerciseRequest(1L, 1, null, null);
        RoutineRequest updateRequest = new RoutineRequest("Push Day", DayOfWeek.MONDAY, null, null, List.of(newExerciseReq));

        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineResponse expectedResponse = new RoutineResponse(1L, "Push Day", DayOfWeek.MONDAY, null,
                null, LocalDateTime.now(), Collections.emptyList());

        when(routineRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(routineMapper.toResponse(existingEntity)).thenReturn(expectedResponse);

        RoutineResponse result = routineService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(exerciseTemplateRepository).findById(1L);
    }

    // --- delete ---

    @Test
    void should_deleteRoutine_when_idExists() {
        when(routineRepository.existsById(1L)).thenReturn(true);

        routineService.delete(1L);

        verify(routineRepository).deleteById(1L);
    }

    @Test
    void should_throwException_when_deletingNonExistentRoutine() {
        when(routineRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> routineService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(routineRepository, never()).deleteById(any());
    }

    // --- helpers ---

    private RoutineEntity buildRoutineEntity(Long id, String name, DayOfWeek day) {
        return RoutineEntity.builder()
                .id(id)
                .name(name)
                .dayOfWeek(day)
                .createdAt(LocalDateTime.now())
                .routineExercises(new ArrayList<>())
                .build();
    }

    private RoutineResponse buildRoutineResponse(Long id, String name, DayOfWeek day) {
        return new RoutineResponse(id, name, day, null, null, LocalDateTime.now(), Collections.emptyList());
    }

    private ExerciseTemplateEntity buildTemplate(Long id, String name, MuscleGroup muscleGroup) {
        return ExerciseTemplateEntity.builder()
                .id(id)
                .name(name)
                .muscleGroup(muscleGroup)
                .build();
    }
}
