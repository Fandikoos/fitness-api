package com.fitness.workout.service.impl;

import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.mapper.ExerciseTemplateMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseTemplateServiceImplTest {

    @Mock
    private ExerciseTemplateRepository exerciseTemplateRepository;

    @Mock
    private ExerciseTemplateMapper exerciseTemplateMapper;

    @InjectMocks
    private ExerciseTemplateServiceImpl exerciseTemplateService;

    // --- create ---

    @Test
    void should_createTemplate_when_validRequest() {
        ExerciseTemplateRequest request = new ExerciseTemplateRequest("Press Banca", MuscleGroup.CHEST, null, null);
        ExerciseTemplateEntity entity = buildEntity(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateResponse expectedResponse = buildResponse(1L, "Press Banca", MuscleGroup.CHEST);

        when(exerciseTemplateMapper.toEntity(request)).thenReturn(entity);
        when(exerciseTemplateRepository.save(entity)).thenReturn(entity);
        when(exerciseTemplateMapper.toResponse(entity)).thenReturn(expectedResponse);

        ExerciseTemplateResponse result = exerciseTemplateService.create(request);

        assertThat(result.name()).isEqualTo("Press Banca");
        assertThat(result.muscleGroup()).isEqualTo(MuscleGroup.CHEST);
        verify(exerciseTemplateRepository).save(entity);
    }

    // --- findById ---

    @Test
    void should_returnTemplate_when_idExists() {
        ExerciseTemplateEntity entity = buildEntity(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateResponse expectedResponse = buildResponse(1L, "Press Banca", MuscleGroup.CHEST);

        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(exerciseTemplateMapper.toResponse(entity)).thenReturn(expectedResponse);

        ExerciseTemplateResponse result = exerciseTemplateService.findById(1L);

        assertThat(result.name()).isEqualTo("Press Banca");
    }

    @Test
    void should_throwException_when_templateNotFound() {
        when(exerciseTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseTemplateService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- findAll ---

    @Test
    void should_returnAllTemplates_when_templatesExist() {
        ExerciseTemplateEntity entity1 = buildEntity(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateEntity entity2 = buildEntity(2L, "Dominadas", MuscleGroup.BACK);
        ExerciseTemplateResponse response1 = buildResponse(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateResponse response2 = buildResponse(2L, "Dominadas", MuscleGroup.BACK);

        when(exerciseTemplateRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(exerciseTemplateMapper.toResponse(entity1)).thenReturn(response1);
        when(exerciseTemplateMapper.toResponse(entity2)).thenReturn(response2);

        List<ExerciseTemplateResponse> result = exerciseTemplateService.findAll();

        assertThat(result).hasSize(2);
    }

    // --- findByMuscleGroup ---

//    @Test
//    void should_returnTemplates_when_filterByMuscleGroup() {
//        ExerciseTemplateEntity entity = buildEntity(1L, "Press Banca", MuscleGroup.CHEST);
//        ExerciseTemplateResponse response = buildResponse(1L, "Press Banca", MuscleGroup.CHEST);
//
//        when(exerciseTemplateRepository.findByMuscleGroup(MuscleGroup.CHEST)).thenReturn(List.of(entity));
//        when(exerciseTemplateMapper.toResponse(entity)).thenReturn(response);
//
//        List<ExerciseTemplateResponse> result = exerciseTemplateService.findByMuscleGroup(MuscleGroup.CHEST);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.getFirst().muscleGroup()).isEqualTo(MuscleGroup.CHEST);
//    }

    // --- update ---

    @Test
    void should_updateTemplate_when_idExists() {
        ExerciseTemplateEntity entity = buildEntity(1L, "Press Banca", MuscleGroup.CHEST);
        ExerciseTemplateRequest request = new ExerciseTemplateRequest("Press Inclinado", MuscleGroup.CHEST, "img.png", null);
        ExerciseTemplateResponse expectedResponse = new ExerciseTemplateResponse(1L, "Press Inclinado", MuscleGroup.CHEST, "img.png", null);

        when(exerciseTemplateRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(exerciseTemplateMapper.toResponse(entity)).thenReturn(expectedResponse);

        ExerciseTemplateResponse result = exerciseTemplateService.update(1L, request);

        assertThat(result.name()).isEqualTo("Press Inclinado");
        verify(exerciseTemplateRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_updatingNonExistentTemplate() {
        ExerciseTemplateRequest request = new ExerciseTemplateRequest("Press Banca", MuscleGroup.CHEST, null, null);

        when(exerciseTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseTemplateService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void should_deleteTemplate_when_idExists() {
        when(exerciseTemplateRepository.existsById(1L)).thenReturn(true);

        exerciseTemplateService.delete(1L);

        verify(exerciseTemplateRepository).deleteById(1L);
    }

    @Test
    void should_throwException_when_deletingNonExistentTemplate() {
        when(exerciseTemplateRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> exerciseTemplateService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(exerciseTemplateRepository, never()).deleteById(any());
    }

    // --- helpers ---

    private ExerciseTemplateEntity buildEntity(Long id, String name, MuscleGroup muscleGroup) {
        return ExerciseTemplateEntity.builder()
                .id(id)
                .name(name)
                .muscleGroup(muscleGroup)
                .build();
    }

    private ExerciseTemplateResponse buildResponse(Long id, String name, MuscleGroup muscleGroup) {
        return new ExerciseTemplateResponse(id, name, muscleGroup, null, null);
    }
}
