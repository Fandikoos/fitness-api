package com.fitness.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un programa de entrenamiento completo (ej. "Push Pull Legs", "Full Body 3x").
 *
 * Un ProgramEntity es el nivel más alto de organización: agrupa varias rutinas (días)
 * bajo un mismo plan. El usuario puede tener varios programas pero solo uno activo
 * a la vez, lo que permite alternar entre planes sin perder los anteriores.
 *
 * Jerarquía: Program 1──N Routine 1──N Exercise 1──N ExerciseSet
 */
@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del programa (ej. "Push Pull Legs", "Upper Lower", "Full Body 3x")
    @Column(nullable = false)
    private String name;

    // Descripción opcional del programa (ej. "Programa de hipertrofia de 6 semanas")
    @Column
    private String description;

    // Indica si este es el programa que el usuario está siguiendo actualmente.
    // Permite tener varios programas guardados pero solo uno en uso.
    @Column(nullable = false)
    @Builder.Default
    private boolean active = false;

    // cascade ALL: las operaciones sobre el programa se propagan a sus rutinas.
    // orphanRemoval: si se quita una rutina de la lista, JPA la borra de la BD.
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineEntity> routines = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @PrePersist se ejecuta automáticamente justo antes del primer INSERT en la BD.
    // Así garantizamos que createdAt siempre se rellena sin depender del servicio.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
