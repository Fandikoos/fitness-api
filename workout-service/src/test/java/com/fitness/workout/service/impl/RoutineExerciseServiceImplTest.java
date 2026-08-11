package com.fitness.workout.service.impl;

import com.fitness.workout.dto.*;
import com.fitness.workout.entity.ExerciseSetEntity;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.entity.RoutineExerciseEntity;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.mapper.ExerciseSetMapper;
import com.fitness.workout.mapper.RoutineExerciseMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.repository.RoutineExerciseRepository;
import com.fitness.workout.repository.RoutineRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineExerciseServiceImplTest {

    @Mock
    private RoutineExerciseRepository routineExerciseRepository;

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private ExerciseTemplateRepository exerciseTemplateRepository;

    @Mock
    private RoutineExerciseMapper routineExerciseMapper;

    @Mock
    private ExerciseSetMapper exerciseSetMapper;

    @InjectMocks
    private RoutineExerciseServiceImpl routineExerciseService;

    // --- addToRoutine ---

    @Test
    void should_addExerciseToRoutine_when_routineAndTemplateExist() {
        RoutineEntity routine = RoutineEntity.builder().id(1L).name("Push Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineExerciseRequest request = new RoutineExerciseRequest(1L, 1, "Agarre medio", null);

        RoutineExerciseEntity savedEntity = buildRoutineExercise(1L, template, routine, 1);
        RoutineExerciseResponse expectedResponse = buildResponse(1L, "Press Banca", MuscleGroup.CHEST, 1, "Agarre medio");

        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(routineExerciseRepository.save(any(RoutineExerciseEntity.class))).thenReturn(savedEntity);
        when(routineExerciseMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        RoutineExerciseResponse result = routineExerciseService.addToRoutine(1L, request);

        assertThat(result.exerciseTemplate().name()).isEqualTo("Press Banca");
        assertThat(result.orderInRoutine()).isEqualTo(1);
        verify(routineExerciseRepository).save(any(RoutineExerciseEntity.class));
    }

    @Test
    void should_throwException_when_routineNotFound() {
        RoutineExerciseRequest request = new RoutineExerciseRequest(1L, 1, null, null);

        when(routineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineExerciseService.addToRoutine(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(routineExerciseRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_templateNotFound() {
        RoutineEntity routine = RoutineEntity.builder().id(1L).name("Push Day").build();
        RoutineExerciseRequest request = new RoutineExerciseRequest(99L, 1, null, null);

        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
        when(exerciseTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineExerciseService.addToRoutine(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(routineExerciseRepository, never()).save(any());
    }

    @Test
    void should_addExerciseWithSets_when_requestHasSets() {
        RoutineEntity routine = RoutineEntity.builder().id(1L).name("Push Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);

        ExerciseSetRequest setReq = new ExerciseSetRequest(1, new BigDecimal("80.00"), 10, 8);
        RoutineExerciseRequest request = new RoutineExerciseRequest(1L, 1, null, List.of(setReq));

        ExerciseSetEntity setEntity = ExerciseSetEntity.builder()
                .setNumber(1).weightKg(new BigDecimal("80.00")).reps(10).rpe(8).build();
        RoutineExerciseEntity savedEntity = buildRoutineExercise(1L, template, routine, 1);

        ExerciseSetResponse setResponse = new ExerciseSetResponse(1L, 1, new BigDecimal("80.00"), 10, 8);
        RoutineExerciseResponse expectedResponse = new RoutineExerciseResponse(1L,
                new ExerciseTemplateResponse(1L, "Press Banca", MuscleGroup.CHEST, null, null),
                1, null, List.of(setResponse));

        when(routineRepository.findById(1L)).thenReturn(Optional.of(routine));
        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(exerciseSetMapper.toEntity(setReq)).thenReturn(setEntity);
        when(routineExerciseRepository.save(any(RoutineExerciseEntity.class))).thenReturn(savedEntity);
        when(routineExerciseMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        RoutineExerciseResponse result = routineExerciseService.addToRoutine(1L, request);

        assertThat(result.sets()).hasSize(1);
        assertThat(result.sets().getFirst().weightKg()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    // --- findByRoutineId ---

    @Test
    void should_returnExercises_when_routineExists() {
        ExerciseTemplateEntity template1 = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateEntity template2 = buildTemplate(2L, "Aperturas", MuscleGroup.CHEST);
        RoutineEntity routine = RoutineEntity.builder().id(1L).build();

        RoutineExerciseEntity entity1 = buildRoutineExercise(1L, template1, routine, 1);
        RoutineExerciseEntity entity2 = buildRoutineExercise(2L, template2, routine, 2);

        RoutineExerciseResponse response1 = buildResponse(1L, "Press Banca", MuscleGroup.CHEST, 1, null);
        RoutineExerciseResponse response2 = buildResponse(2L, "Aperturas", MuscleGroup.CHEST, 2, null);

        when(routineRepository.existsById(1L)).thenReturn(true);
        when(routineExerciseRepository.findByRoutineIdOrderByOrderInRoutineAsc(1L))
                .thenReturn(List.of(entity1, entity2));
        when(routineExerciseMapper.toResponse(entity1)).thenReturn(response1);
        when(routineExerciseMapper.toResponse(entity2)).thenReturn(response2);

        List<RoutineExerciseResponse> result = routineExerciseService.findByRoutineId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderInRoutine()).isEqualTo(1);
        assertThat(result.get(1).orderInRoutine()).isEqualTo(2);
    }

    @Test
    void should_throwException_when_findingExercisesOfNonExistentRoutine() {
        when(routineRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> routineExerciseService.findByRoutineId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void should_returnEmptyList_when_routineHasNoExercises() {
        when(routineRepository.existsById(1L)).thenReturn(true);
        when(routineExerciseRepository.findByRoutineIdOrderByOrderInRoutineAsc(1L))
                .thenReturn(Collections.emptyList());

        List<RoutineExerciseResponse> result = routineExerciseService.findByRoutineId(1L);

        assertThat(result).isEmpty();
    }

    // --- update ---

    @Test
    void should_updateRoutineExercise_when_belongsToRoutine() {
        RoutineEntity routine = RoutineEntity.builder().id(1L).name("Push Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineExerciseEntity existingEntity = buildRoutineExercise(5L, template, routine, 1);

        RoutineExerciseRequest updateRequest = new RoutineExerciseRequest(1L, 2, "30 grados", null);
        RoutineExerciseResponse expectedResponse = buildResponse(5L, "Press Banca", MuscleGroup.CHEST, 2, "30 grados");

        when(routineExerciseRepository.findById(5L)).thenReturn(Optional.of(existingEntity));
        when(routineExerciseMapper.toResponse(existingEntity)).thenReturn(expectedResponse);

        RoutineExerciseResponse result = routineExerciseService.update(1L, 5L, updateRequest);

        assertThat(result.orderInRoutine()).isEqualTo(2);
        assertThat(result.notes()).isEqualTo("30 grados");
        // Dirty checking: no se llama a save()
        verify(routineExerciseRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_exerciseDoesNotBelongToRoutine() {
        RoutineEntity otherRoutine = RoutineEntity.builder().id(2L).name("Pull Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineExerciseEntity entity = buildRoutineExercise(5L, template, otherRoutine, 1);

        RoutineExerciseRequest request = new RoutineExerciseRequest(1L, 1, null, null);

        when(routineExerciseRepository.findById(5L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> routineExerciseService.update(1L, 5L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("5")
                .hasMessageContaining("1");
    }

    @Test
    void should_throwException_when_updatingNonExistentExercise() {
        RoutineExerciseRequest request = new RoutineExerciseRequest(1L, 1, null, null);

        when(routineExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineExerciseService.update(1L, 99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void should_deleteExercise_when_belongsToRoutine() {
        RoutineEntity routine = RoutineEntity.builder().id(1L).name("Push Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineExerciseEntity entity = buildRoutineExercise(5L, template, routine, 1);

        when(routineExerciseRepository.findById(5L)).thenReturn(Optional.of(entity));

        routineExerciseService.delete(1L, 5L);

        verify(routineExerciseRepository).delete(entity);
    }

    @Test
    void should_throwException_when_deletingExerciseFromWrongRoutine() {
        RoutineEntity otherRoutine = RoutineEntity.builder().id(2L).name("Pull Day").build();
        ExerciseTemplateEntity template = buildTemplate(1L, "Press Banca", MuscleGroup.CHEST);
        RoutineExerciseEntity entity = buildRoutineExercise(5L, template, otherRoutine, 1);

        when(routineExerciseRepository.findById(5L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> routineExerciseService.delete(1L, 5L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(routineExerciseRepository, never()).delete(any());
    }

    @Test
    void should_throwException_when_deletingNonExistentExercise() {
        when(routineExerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routineExerciseService.delete(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- helpers ---

    private ExerciseTemplateEntity buildTemplate(Long id, String name, MuscleGroup muscleGroup) {
        return ExerciseTemplateEntity.builder()
                .id(id)
                .name(name)
                .muscleGroup(muscleGroup)
                .build();
    }

    private RoutineExerciseEntity buildRoutineExercise(Long id, ExerciseTemplateEntity template,
                                                       RoutineEntity routine, Integer order) {
        return RoutineExerciseEntity.builder()
                .id(id)
                .exerciseTemplate(template)
                .routine(routine)
                .orderInRoutine(order)
                .sets(new ArrayList<>())
                .build();
    }

    private RoutineExerciseResponse buildResponse(Long id, String templateName,
                                                   MuscleGroup muscleGroup, Integer order, String notes) {
        return new RoutineExerciseResponse(id,
                new ExerciseTemplateResponse(null, templateName, muscleGroup, null, null),
                order, notes, Collections.emptyList());
    }
}
