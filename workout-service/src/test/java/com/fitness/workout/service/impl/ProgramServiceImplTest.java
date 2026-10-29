package com.fitness.workout.service.impl;

import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;
import com.fitness.workout.entity.ProgramEntity;
import com.fitness.workout.exception.EntityNotFoundException;
import com.fitness.workout.mapper.ProgramMapper;
import com.fitness.workout.repository.ProgramRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProgramServiceImpl.
 *
 * Se prueban las dos ramas clave de create y update:
 *  - active == null  → no se toca el campo ni se buscan programas activos
 *  - active == false → se actualiza el campo pero no se desactiva nada
 *  - active == true  → se desactivan los programas activos antes de activar el nuevo
 *
 * También se verifica el dirty checking en update: JPA detecta los cambios
 * automáticamente, por lo que save() NO debe llamarse explícitamente.
 */
@ExtendWith(MockitoExtension.class)
class ProgramServiceImplTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramMapper programMapper;

    @InjectMocks
    private ProgramServiceImpl programService;

    // =========================================================================
    // create
    // =========================================================================

    @Test
    void should_createProgram_when_activeIsNull() {
        // active == null → la entidad conserva el false por defecto del @Builder.Default
        ProgramRequest request = new ProgramRequest("PPL", "Push Pull Legs", null);
        ProgramEntity entity = buildEntity(null, "PPL", false);
        ProgramEntity saved  = buildEntity(1L,   "PPL", false);
        ProgramResponse expected = buildResponse(1L, "PPL", false);

        when(programMapper.toEntity(request)).thenReturn(entity);
        when(programRepository.save(entity)).thenReturn(saved);
        when(programMapper.toResponse(saved)).thenReturn(expected);

        ProgramResponse result = programService.create(request);

        assertThat(result).isEqualTo(expected);
        verify(programRepository).save(entity);
        // No debe consultarse ni tocarse ningún programa activo
        verify(programRepository, never()).findByActiveTrue();
    }

    @Test
    void should_createProgram_when_activeIsFalse() {
        // active == false → se setea explícitamente en falso pero no se desactiva nadie
        ProgramRequest request = new ProgramRequest("PPL", null, false);
        ProgramEntity entity = buildEntity(null, "PPL", false);
        ProgramEntity saved  = buildEntity(1L,   "PPL", false);
        ProgramResponse expected = buildResponse(1L, "PPL", false);

        when(programMapper.toEntity(request)).thenReturn(entity);
        when(programRepository.save(entity)).thenReturn(saved);
        when(programMapper.toResponse(saved)).thenReturn(expected);

        programService.create(request);

        assertThat(entity.isActive()).isFalse();
        verify(programRepository, never()).findByActiveTrue();
    }

    @Test
    void should_deactivateOtherPrograms_when_creatingWithActiveTrue() {
        // active == true → se debe desactivar cualquier programa que ya estuviera activo
        ProgramRequest request = new ProgramRequest("PPL", null, true);
        ProgramEntity entity        = buildEntity(null, "PPL",       false);
        ProgramEntity previousActive = buildEntity(2L,  "Full Body", true);
        ProgramEntity saved          = buildEntity(1L,  "PPL",       true);
        ProgramResponse expected     = buildResponse(1L, "PPL",      true);

        when(programMapper.toEntity(request)).thenReturn(entity);
        when(programRepository.findByActiveTrue()).thenReturn(List.of(previousActive));
        when(programRepository.save(entity)).thenReturn(saved);
        when(programMapper.toResponse(saved)).thenReturn(expected);

        ProgramResponse result = programService.create(request);

        assertThat(result).isEqualTo(expected);
        // El programa previamente activo debe haberse desactivado (dirty checking en su propia transacción)
        assertThat(previousActive.isActive()).isFalse();
        // El nuevo programa queda activo
        assertThat(entity.isActive()).isTrue();
        verify(programRepository).findByActiveTrue();
        verify(programRepository).save(entity);
    }

    @Test
    void should_createProgram_when_activeTrueButNoActivePrograms() {
        // active == true pero no hay ningún programa activo → se activa directamente sin nada que desactivar
        ProgramRequest request = new ProgramRequest("PPL", null, true);
        ProgramEntity entity = buildEntity(null, "PPL", false);
        ProgramEntity saved  = buildEntity(1L,   "PPL", true);
        ProgramResponse expected = buildResponse(1L, "PPL", true);

        when(programMapper.toEntity(request)).thenReturn(entity);
        when(programRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        when(programRepository.save(entity)).thenReturn(saved);
        when(programMapper.toResponse(saved)).thenReturn(expected);

        programService.create(request);

        assertThat(entity.isActive()).isTrue();
        verify(programRepository).findByActiveTrue();
    }

    // =========================================================================
    // update
    // =========================================================================

    @Test
    void should_throwException_when_updatingNonExistentProgram() {
        ProgramRequest request = new ProgramRequest("PPL", null, null);
        when(programRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void should_updateNameAndDescription_when_activeIsNull() {
        // active == null → solo se actualizan nombre y descripción; no se desactiva nada
        ProgramRequest request = new ProgramRequest("PPL v2", "Actualizado", null);
        ProgramEntity entity  = buildEntity(1L, "PPL", false);
        ProgramResponse expected = buildResponse(1L, "PPL v2", false);

        when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(programMapper.toResponse(entity)).thenReturn(expected);

        ProgramResponse result = programService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("PPL v2");
        assertThat(entity.getDescription()).isEqualTo("Actualizado");
        verify(programRepository, never()).findByActiveTrue();
        // Dirty checking: la entidad es "managed", JPA detecta los cambios solo; no se llama a save()
        verify(programRepository, never()).save(any());
    }

    @Test
    void should_setActiveFalse_when_updatingWithActiveFalse() {
        // active == false → se desactiva el programa sin tocar ningún otro
        ProgramRequest request = new ProgramRequest("PPL", null, false);
        ProgramEntity entity  = buildEntity(1L, "PPL", true); // estaba activo
        ProgramResponse expected = buildResponse(1L, "PPL", false);

        when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(programMapper.toResponse(entity)).thenReturn(expected);

        programService.update(1L, request);

        assertThat(entity.isActive()).isFalse();
        verify(programRepository, never()).findByActiveTrue();
        verify(programRepository, never()).save(any());
    }

    @Test
    void should_deactivateOtherPrograms_when_updatingWithActiveTrue() {
        // active == true → se desactivan los programas activos antes de activar este
        ProgramRequest request = new ProgramRequest("PPL", null, true);
        ProgramEntity entity         = buildEntity(1L, "PPL",       false);
        ProgramEntity otherActive    = buildEntity(2L, "Full Body", true);
        ProgramResponse expected     = buildResponse(1L, "PPL",     true);

        when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(programRepository.findByActiveTrue()).thenReturn(List.of(otherActive));
        when(programMapper.toResponse(entity)).thenReturn(expected);

        programService.update(1L, request);

        assertThat(otherActive.isActive()).isFalse();
        assertThat(entity.isActive()).isTrue();
        verify(programRepository).findByActiveTrue();
        // Dirty checking: no se llama a save() para ninguno de los dos
        verify(programRepository, never()).save(any());
    }

    @Test
    void should_updateProgram_when_activeTrueButNoActivePrograms() {
        // active == true pero no hay programas activos previos
        ProgramRequest request = new ProgramRequest("PPL", null, true);
        ProgramEntity entity = buildEntity(1L, "PPL", false);
        ProgramResponse expected = buildResponse(1L, "PPL", true);

        when(programRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(programRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        when(programMapper.toResponse(entity)).thenReturn(expected);

        programService.update(1L, request);

        assertThat(entity.isActive()).isTrue();
        verify(programRepository).findByActiveTrue();
        verify(programRepository, never()).save(any());
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private ProgramEntity buildEntity(Long id, String name, boolean active) {
        return ProgramEntity.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }

    private ProgramResponse buildResponse(Long id, String name, boolean active) {
        return new ProgramResponse(id, name, null, active, LocalDateTime.now(), Collections.emptyList());
    }
}