package com.fitness.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Representa una serie individual de un ejercicio (ej. "Serie 2: 80kg x 10 reps, RPE 8").
 *
 * Se separa del ejercicio porque cada serie puede tener distinto peso,
 * repeticiones y nivel de esfuerzo. Esto permite modelar correctamente
 * entrenamientos reales donde subes o bajas peso entre series.
 */
@Entity
@Table(name = "exercise_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número de serie dentro del ejercicio (1, 2, 3...).
    // Permite ordenar las series y saber cuál es cuál.
    @Column(nullable = false)
    private Integer setNumber;

    // BigDecimal para el peso porque evita errores de redondeo en coma flotante.
    // Ejemplo: con Double, 62.5 podría guardarse como 62.4999... internamente.
    // precision = 5, scale = 2 permite hasta 999.99 kg, más que suficiente.
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    // Número de repeticiones realizadas en esta serie.
    @Column(nullable = false)
    private Integer reps;

    // RPE (Rate of Perceived Exertion): escala subjetiva del 1 al 10 que mide
    // lo duro que fue la serie. Un RPE 10 = fallo muscular total.
    // Nullable porque no siempre se registra, es opcional.
    @Column
    private Integer rpe;

    // @ManyToOne con LAZY: no carga el ejercicio completo salvo que se necesite.
    // @JoinColumn define la FK en la tabla exercise_sets que apunta a routine_exercises.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_exercise_id", nullable = false)
    private RoutineExerciseEntity routineExercise;
}
