package com.fitness.workout.repository;

import com.fitness.workout.entity.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a los programas de entrenamiento en la BD.
 *
 * JpaRepository ya proporciona CRUD completo (save, findById, findAll, delete...).
 * Los métodos extra usan "derived queries": Spring Data genera el SQL automáticamente
 * a partir del nombre del método.
 */
@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {

    // Buscar programas activos (normalmente solo debería haber uno)
    Optional<ProgramEntity> findByActiveTrue();

    // Buscar un programa por nombre exacto (útil para evitar duplicados)
    Optional<ProgramEntity> findByName(String name);
}
