package com.fitness.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la asignación de un ejercicio del catálogo a una rutina concreta.
 *
 * Esta entidad es el "puente" entre ExerciseTemplateEntity (catálogo) y
 * RoutineEntity (día de entrenamiento). Contiene los datos específicos de
 * ese ejercicio en esa rutina: orden, notas personalizadas y las series (sets).
 *
 * Separar esta entidad del catálogo permite que el mismo ejercicio ("Press Banca")
 * aparezca en varias rutinas con distintas notas, orden y series.
 */
@Entity
@Table(name = "routine_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al ejercicio del catálogo. LAZY para no cargar la plantilla
    // completa cada vez que accedemos al ejercicio de una rutina.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_template_id", nullable = false)
    private ExerciseTemplateEntity exerciseTemplate;

    // Rutina a la que pertenece este ejercicio.
    // @ManyToOne con LAZY: no carga la rutina completa salvo que se necesite.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineEntity routine;

    // Posición del ejercicio dentro de la rutina (1, 2, 3...).
    // Permite reordenar los ejercicios sin modificar sus datos.
    @Column(nullable = false)
    private Integer orderInRoutine;

    // Notas o comentarios específicos para esta rutina (ej. "Agarre cerrado", "Bajar lento").
    // Son independientes del catálogo: distintas rutinas pueden tener distintas notas
    // para el mismo ejercicio.
    @Column
    private String notes;

    // cascade ALL + orphanRemoval: las series se gestionan completamente
    // desde el ejercicio en rutina. Si borras el ejercicio de la rutina, se borran sus series.
    @OneToMany(mappedBy = "routineExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExerciseSetEntity> sets = new ArrayList<>();
}
