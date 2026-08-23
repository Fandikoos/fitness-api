package com.fitness.workout.service.impl;

import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.dto.PageResponse;
import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.mapper.ExerciseTemplateMapper;
import com.fitness.workout.repository.ExerciseTemplateRepository;
import com.fitness.workout.service.ExerciseTemplateService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de catálogo de ejercicios.
 *
 * Gestiona el CRUD de ejercicios "plantilla" que existen independientemente
 * de cualquier rutina. El frontend los lista para que el usuario elija
 * cuáles añadir a su rutina.
 */
@Service
@Transactional
public class ExerciseTemplateServiceImpl implements ExerciseTemplateService {

    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final ExerciseTemplateMapper exerciseTemplateMapper;

    public ExerciseTemplateServiceImpl(ExerciseTemplateRepository exerciseTemplateRepository,
                                       ExerciseTemplateMapper exerciseTemplateMapper) {
        this.exerciseTemplateRepository = exerciseTemplateRepository;
        this.exerciseTemplateMapper = exerciseTemplateMapper;
    }

    @Override
    public ExerciseTemplateResponse create(ExerciseTemplateRequest request) {
        ExerciseTemplateEntity entity = exerciseTemplateMapper.toEntity(request);
        ExerciseTemplateEntity saved = exerciseTemplateRepository.save(entity);
        return exerciseTemplateMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseTemplateResponse findById(Long id) {
        ExerciseTemplateEntity entity = exerciseTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el ejercicio con id: " + id));
        return exerciseTemplateMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseTemplateResponse> findAll() {
        return exerciseTemplateRepository.findAll().stream()
                .map(exerciseTemplateMapper::toResponse)
                .toList();
    }

    @Override
    public ExerciseTemplateResponse update(Long id, ExerciseTemplateRequest request) {
        ExerciseTemplateEntity entity = exerciseTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el ejercicio con id: " + id));

        // Dirty checking: actualizamos los campos y JPA detecta los cambios automáticamente
        entity.setName(request.name());
        entity.setMuscleGroup(request.muscleGroup());
        entity.setImageUrl(request.imageUrl());
        entity.setVideoUrl(request.videoUrl());

        return exerciseTemplateMapper.toResponse(entity);
    }

    @Override
    public PageResponse<ExerciseTemplateResponse> findByMuscleGroup(MuscleGroup muscleGroup, Pageable pageable) {
        Page<ExerciseTemplateEntity> exerciseTemplateEntities = exerciseTemplateRepository.findByMuscleGroup(muscleGroup, pageable);
        List<ExerciseTemplateResponse> content = exerciseTemplateEntities.map(exerciseTemplateMapper::toResponse).getContent();
        return new PageResponse<>(
                content,
                exerciseTemplateEntities.getNumber(),
                exerciseTemplateEntities.getSize(),
                exerciseTemplateEntities.getTotalElements(),
                exerciseTemplateEntities.getTotalPages(),
                exerciseTemplateEntities.isFirst(),
                exerciseTemplateEntities.isLast()
        );
    }

    @Override
    public void delete(Long id) {
        if (!exerciseTemplateRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se encontró el ejercicio con id: " + id);
        }
        exerciseTemplateRepository.deleteById(id);
    }
}
