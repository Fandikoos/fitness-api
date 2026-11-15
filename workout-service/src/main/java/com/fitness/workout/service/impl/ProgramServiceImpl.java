package com.fitness.workout.service.impl;

import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;
import com.fitness.workout.entity.ProgramEntity;
import com.fitness.workout.exception.EntityNotFoundException;
import com.fitness.workout.exception.NotExistActiveProgramException;
import com.fitness.workout.mapper.ProgramMapper;
import com.fitness.workout.repository.ProgramRepository;
import com.fitness.workout.service.ProgramService;
import com.fitness.workout.util.ListUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de programas de entrenamiento.
 *
 * @Service marca esta clase como un bean de la capa de servicio.
 * Spring lo detecta automáticamente y lo registra en el contexto de la app.
 *
 * @Transactional a nivel de clase hace que todos los métodos públicos
 * se ejecuten dentro de una transacción de BD. Si algo falla, se hace rollback
 * automático y la BD queda en un estado consistente.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramMapper programMapper;

    @Override
    public ProgramResponse create(ProgramRequest request) {
        ProgramEntity program = programMapper.toEntity(request);

        // Si el request no envía "active", el campo queda en false por defecto
        // gracias al @Builder.Default de la entidad.
        if (request.active() != null) {
            if (Boolean.TRUE.equals(request.active())) {
                // Si se activa un programa, desactivamos los demás activos
                deactivePossiblePrograms();
            }
            program.setActive(request.active());
        }

        ProgramEntity saved = programRepository.save(program);
        return programMapper.toResponse(saved);
    }

    // readOnly = true optimiza la transacción: Hibernate no necesita
    // comprobar cambios (dirty checking) al final, mejorando el rendimiento.
    @Override
    @Transactional(readOnly = true)
    public ProgramResponse findById(Long id) {
        ProgramEntity program = programRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el programa con id: " + id));
        return programMapper.toResponse(program);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramResponse> findAll() {
        return programRepository.findAll().stream()
                .map(programMapper::toResponse)
                .toList();
    }

    @Override
    public ProgramResponse update(Long id, ProgramRequest request) {
        ProgramEntity program = programRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el programa con id: " + id));

        // Actualizamos los campos simples del programa
        program.setName(request.name());
        program.setDescription(request.description());

        if (request.active() != null) {
            if (Boolean.TRUE.equals(request.active())) {
                // Si se activa un programa, desactivamos los demás activos
                deactivePossiblePrograms();
            }
            program.setActive(request.active());
        }

        // No necesitamos llamar a save() explícitamente: como la entidad está
        // "managed" (cargada dentro de la transacción), JPA detecta los cambios
        // automáticamente al final de la transacción (dirty checking).
        return programMapper.toResponse(program);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramResponse findActive() {
        Optional<ProgramEntity> activeProgram = programRepository.findByActiveTrue();
        if (activeProgram.isEmpty()) {
            throw new NotExistActiveProgramException("No hay programas activos actualmente");
        }
        return programMapper.toResponse(activeProgram.get());
    }

    @Override
    public void delete(Long id) {
        if (!programRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se encontró el programa con id: " + id);
        }
        // deleteById borra el programa y, gracias a cascade ALL,
        // también todas sus rutinas, ejercicios y series.
        programRepository.deleteById(id);
    }

    private void deactivePossiblePrograms() {
        Optional<ProgramEntity> activePrograms = programRepository.findByActiveTrue();
        if (activePrograms.isEmpty()) {
            log.debug("No hay programas activos, no es necesario desactivar ninguno");
            return;
        }

        activePrograms.ifPresent(program -> {
            program.setActive(false);
            log.debug("Programa con id {} desactivado", program.getId());
        });
    }
}
