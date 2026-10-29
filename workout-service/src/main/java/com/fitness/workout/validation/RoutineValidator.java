package com.fitness.workout.validation;

import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.enums.DayOfWeek;
import com.fitness.workout.exception.BusinessValidationException;
import com.fitness.workout.exception.ConflictException;
import com.fitness.workout.repository.RoutineRepository;
import com.fitness.workout.util.ListUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Centraliza todas las validaciones de negocio del dominio Routine.
 *
 * Separamos las validaciones del servicio siguiendo SRP (Single Responsibility Principle):
 * el servicio orquesta el flujo, el validador garantiza las reglas de negocio.
 * Esto también facilita testear cada regla de forma aislada.
 *
 * @Component lo registra como bean de Spring para poder inyectarlo donde haga falta.
 */
@Component
public class RoutineValidator {

    private final RoutineRepository routineRepository;

    public RoutineValidator(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }

    /**
     * Valida todas las reglas de negocio al crear una rutina.
     */
    public void validateForCreate(RoutineRequest request) {
        validateNoDuplicateDayInProgram(request.programId(), request.dayOfWeek(), null);
        validateExercises(request.exercises());
    }

    /**
     * Valida todas las reglas de negocio al actualizar una rutina.
     *
     * Se pasa el routineId para excluirlo de la búsqueda de duplicados:
     * si la rutina mantiene el mismo día y programa, no debe chocar consigo misma.
     */
    public void validateForUpdate(Long routineId, RoutineRequest request) {
        validateNoDuplicateDayInProgram(request.programId(), request.dayOfWeek(), routineId);
        validateExercises(request.exercises());
    }

    /**
     * Comprueba que no exista ya otra rutina para el mismo día dentro del mismo programa.
     *
     * En create, excludeRoutineId es null (no hay nada que excluir).
     * En update, pasamos el ID de la rutina que se está editando para evitar
     * que choque consigo misma si el día/programa no cambian.
     *
     * Lanza ConflictException (→ 409) porque el conflicto es con un recurso existente,
     * no con los datos del propio request.
     */
    private void validateNoDuplicateDayInProgram(Long programId, DayOfWeek dayOfWeek, Long excludeRoutineId) {
        if (programId == null) return;

        Optional<RoutineEntity> existing = routineRepository.findByProgramIdAndDayOfWeek(programId, dayOfWeek);

        existing.ifPresent(routine -> {
            // En update, ignoramos el resultado si es la propia rutina que estamos editando
            if (routine.getId().equals(excludeRoutineId)) return;

            throw new ConflictException(
                    "Ya existe una rutina para el día " + dayOfWeek +
                    " en el programa con id: " + programId
            );
        });
    }

    /**
     * Valida que los ejercicios de una rutina no tengan templateId ni orden duplicados.
     *
     * Usamos Set.add() como truco eficiente: devuelve false si el elemento ya existía,
     * detectando duplicados en una sola pasada O(n) sin necesitar ordenar ni comparar
     * la lista consigo misma (lo que sería O(n²)).
     *
     * Lanza BusinessValidationException (→ 422) porque el error está dentro del
     * propio request, no en conflicto con datos existentes.
     */
    private void validateExercises(List<RoutineExerciseRequest> exercises) {
        if (ListUtils.isNullOrEmpty(exercises)) return;

        Set<Long> templateIds = new HashSet<>();
        Set<Integer> orders = new HashSet<>();

        for (RoutineExerciseRequest ex : exercises) {
            if (!templateIds.add(ex.exerciseTemplateId())) {
                throw new BusinessValidationException(
                        "No puede haber dos ejercicios iguales en la misma rutina " +
                        "(exerciseTemplateId duplicado: " + ex.exerciseTemplateId() + ")"
                );
            }
            if (!orders.add(ex.orderInRoutine())) {
                throw new BusinessValidationException(
                        "Dos ejercicios no pueden tener el mismo orden en la rutina " +
                        "(orderInRoutine duplicado: " + ex.orderInRoutine() + ")"
                );
            }
        }
    }
}
