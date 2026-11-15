package com.fitness.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "logged_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoggedSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_exercise_id", referencedColumnName = "id", nullable = false)
    private LoggedExerciseEntity loggedExercise;

    @Column(nullable = false)
    private Integer numberSet;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false)
    private Integer reps;

    @Column
    private Integer rpe;
}
