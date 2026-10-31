package com.fitness.workout.entity;

import com.fitness.workout.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un día de rutina de entrenamiento (ej. "Día de Pecho y Tríceps").
 *
 * Cada RoutineEntity agrupa los ejercicios que se hacen un día concreto.
 * Se le asigna un nombre descriptivo y opcionalmente un día de la semana,
 * para poder organizar la rutina semanal completa.
 */
@Entity
@Table(name = "routines", uniqueConstraints = @UniqueConstraint(name = "uq_routine_day_per_program", columnNames = {"program_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre descriptivo del día de rutina (ej. "Push Day", "Espalda y Bíceps")
    @Column(nullable = false)
    private String name;

    // Día de la semana asignado a esta rutina. Nullable porque puede que
    // el usuario aún no haya decidido qué día hacerla.
    // @Enumerated(STRING) guarda el nombre del enum como texto ("MONDAY", "TUESDAY"...)
    // en vez del índice numérico, lo que es más legible y resistente a reordenamientos.
    @Enumerated(EnumType.STRING)
    @Column
    private DayOfWeek dayOfWeek;

    // Descripción opcional para anotar el enfoque del día (ej. "Hipertrofia, alto volumen")
    @Column
    private String description;

    // Programa al que pertenece esta rutina. Nullable porque una rutina
    // puede existir sin estar asignada a ningún programa todavía.
    // LAZY evita cargar el programa entero cada vez que se consulta una rutina.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private ProgramEntity program;

    // cascade ALL: cualquier operación (persist, merge, remove) sobre la rutina
    // se propaga automáticamente a sus ejercicios vinculados.
    // orphanRemoval: si quitas un ejercicio de la lista, JPA lo borra de la BD.
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineExerciseEntity> routineExercises = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @PrePersist se ejecuta automáticamente justo antes del primer INSERT en la BD.
    // Así garantizamos que createdAt siempre se rellena sin depender del servicio.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
