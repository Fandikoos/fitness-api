# MapStruct - Guía para este proyecto

## ¿Qué es MapStruct?

MapStruct es una librería que **genera automáticamente código para convertir entre objetos**
(en nuestro caso, entre Entidades JPA y DTOs). La clave es que genera el código **en tiempo
de compilación**, no en ejecución. Esto significa que:

- Es más rápido que alternativas basadas en reflexión (como ModelMapper)
- Si algo está mal mapeado, te enteras al compilar, no al ejecutar
- Puedes ver exactamente qué código genera (en `target/generated-sources/`)

## ¿Por qué lo necesitamos?

Porque nunca exponemos las entidades JPA directamente en la API. Usamos DTOs
(Data Transfer Objects) para controlar qué datos entran y salen. Sin MapStruct,
tendríamos que escribir a mano código repetitivo como:

```java
// Sin MapStruct: tedioso y propenso a errores
ExerciseSetEntity entity = new ExerciseSetEntity();
entity.setSetNumber(request.setNumber());
entity.setWeightKg(request.weightKg());
entity.setReps(request.reps());
entity.setRpe(request.rpe());
```

Con MapStruct, solo definimos la interfaz y él genera ese código por nosotros.

---

## Anatomía de un mapper

Tomemos `ExerciseSetMapper` como ejemplo (el más simple):

```java
@Mapper(componentModel = "spring")
public interface ExerciseSetMapper {

    ExerciseSetResponse toResponse(ExerciseSetEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    ExerciseSetEntity toEntity(ExerciseSetRequest request);
}
```

### Lo que tú escribes vs. lo que MapStruct genera

Tú solo escribes la **interfaz** (el contrato). MapStruct genera la **implementación**
(`ExerciseSetMapperImpl.java` en `target/generated-sources/`). Este es el código real
que se ejecuta:

```java
// GENERADO AUTOMÁTICAMENTE por MapStruct (ExerciseSetMapperImpl.java)
@Component  // <-- Spring lo registra como bean, se puede inyectar
public class ExerciseSetMapperImpl implements ExerciseSetMapper {

    @Override
    public ExerciseSetResponse toResponse(ExerciseSetEntity entity) {
        if (entity == null) return null;

        // MapStruct matchea por nombre: entity.getSetNumber() → setNumber del record
        Long id = entity.getId();
        Integer setNumber = entity.getSetNumber();
        BigDecimal weightKg = entity.getWeightKg();
        Integer reps = entity.getReps();
        Integer rpe = entity.getRpe();

        return new ExerciseSetResponse(id, setNumber, weightKg, reps, rpe);
    }

    @Override
    public ExerciseSetEntity toEntity(ExerciseSetRequest request) {
        if (request == null) return null;

        // Usa el Builder de Lombok (gracias al lombok-mapstruct-binding)
        // Nota: NO setea id ni exercise porque los marcamos con ignore = true
        ExerciseSetEntity.ExerciseSetEntityBuilder builder = ExerciseSetEntity.builder();
        builder.setNumber(request.setNumber());
        builder.weightKg(request.weightKg());
        builder.reps(request.reps());
        builder.rpe(request.rpe());

        return builder.build();
    }
}
```

### ¿Cómo sabe MapStruct qué campo va con cuál?

**Por nombre.** Si el campo se llama igual en origen y destino, lo mapea automáticamente:
- `request.setNumber()` → `entity.setSetNumber()` (ambos se llaman `setNumber`)
- `request.weightKg()` → `entity.setWeightKg()` (ambos se llaman `weightKg`)

Si los nombres no coincidieran, habría que indicarlo manualmente:
```java
// Ejemplo hipotético: si en el DTO se llamara "peso" pero en la entidad "weightKg"
@Mapping(source = "peso", target = "weightKg")
ExerciseSetEntity toEntity(ExerciseSetRequest request);
```

---

## Anotaciones clave

### `@Mapper(componentModel = "spring")`

Le dice a MapStruct que la clase generada sea un `@Component` de Spring.
Sin esto, MapStruct genera una clase normal y tendrías que instanciarla a mano.

### `@Mapping(target = "id", ignore = true)`

"No mapees este campo, déjalo en null." Lo usamos para:
- **id**: porque JPA lo genera automáticamente con `@GeneratedValue`
- **createdAt**: porque lo setea el `@PrePersist` de la entidad
- **exercise/routine**: porque la relación padre-hijo se establece aparte (ver @AfterMapping)

### `@MappingTarget`

Indica que el método **modifica un objeto existente** en vez de crear uno nuevo:

```java
// SIN @MappingTarget: crea un objeto nuevo
ExerciseSetEntity toEntity(ExerciseSetRequest request);

// CON @MappingTarget: modifica el objeto existente
void updateEntity(ExerciseSetRequest request, @MappingTarget ExerciseSetEntity entity);
```

El segundo es útil para updates: cargas la entidad de la BD, y en vez de crear una nueva,
actualizas los campos de la existente. Así JPA puede hacer dirty checking.

### `uses = {OtroMapper.class}`

Encadena mappers. Cuando MapStruct necesita convertir un tipo que no sabe manejar solo
(como `List<ExerciseSetEntity>` → `List<ExerciseSetResponse>`), busca en los mappers
declarados en `uses` un método que sepa hacer esa conversión.

```java
@Mapper(componentModel = "spring", uses = {ExerciseSetMapper.class})
public interface ExerciseMapper { ... }
```

Esto genera un código donde `ExerciseMapperImpl` inyecta `ExerciseSetMapper` y lo usa:

```java
// Código generado: ExerciseMapperImpl inyecta y usa ExerciseSetMapper
@Autowired
private ExerciseSetMapper exerciseSetMapper;

// Cuando necesita convertir la lista de sets, delega a exerciseSetMapper
List<ExerciseSetResponse> list = new ArrayList<>();
for (ExerciseSetEntity set : entity.getSets()) {
    list.add(exerciseSetMapper.toResponse(set));  // <-- delega aquí
}
```

La cadena completa en nuestro proyecto:
```
RoutineMapper (uses ExerciseMapper)
    └── ExerciseMapper (uses ExerciseSetMapper)
            └── ExerciseSetMapper
```

---

## @AfterMapping y el problema de la bidireccionalidad

### El problema

Cuando MapStruct convierte un `ExerciseRequest` a `ExerciseEntity`, hace esto:

1. Crea el `ExerciseEntity` con nombre, imageUrl, orderInRoutine, notes
2. Convierte cada `ExerciseSetRequest` a `ExerciseSetEntity` (delegando a ExerciseSetMapper)
3. Mete los sets en la lista `exercise.sets`

Pero hay un problema: **los sets no saben a qué ejercicio pertenecen**. Así queda:

```
ExerciseEntity (id=null, name="Press Banca")
  └── sets:
       ├── ExerciseSetEntity (setNumber=1, weightKg=80, exercise=NULL)  ← ¡NULL!
       └── ExerciseSetEntity (setNumber=2, weightKg=85, exercise=NULL)  ← ¡NULL!
```

Si intentas guardar esto en la BD, JPA falla porque `exercise_id` es NOT NULL.
El set necesita saber "yo pertenezco a este ejercicio" para que JPA escriba la FK.

### La solución: @AfterMapping

```java
@AfterMapping
default void setExerciseInSets(@MappingTarget ExerciseEntity entity) {
    if (entity.getSets() != null) {
        entity.getSets().forEach(set -> set.setExercise(entity));
    }
}
```

Este método se ejecuta **justo después** de que MapStruct termine el mapeo automático.
Lo que hace es recorrer cada set y decirle "tu padre es este ejercicio":

```
ExerciseEntity (id=null, name="Press Banca")     ← este objeto
  └── sets:                                         │
       ├── ExerciseSetEntity (exercise= ────────────┘)  ← ahora apunta al padre
       └── ExerciseSetEntity (exercise= ────────────┘)  ← ahora apunta al padre
```

### ¿Por qué MapStruct no puede hacer esto solo?

Porque es una **referencia circular**: el ejercicio contiene los sets, y cada set
referencia al ejercicio. MapStruct no puede setear `set.exercise = exercise` durante
la construcción del ejercicio porque el ejercicio aún no existe (se está construyendo).
Por eso necesitamos el @AfterMapping: primero se crea todo, y después se enlazan.

### Lo mismo pasa un nivel arriba

En `RoutineMapper`, el `@AfterMapping` hace exactamente lo mismo pero entre Routine y Exercises:

```java
@AfterMapping
default void setRoutineInExercises(@MappingTarget RoutineEntity entity) {
    if (entity.getExercises() != null) {
        entity.getExercises().forEach(exercise -> exercise.setRoutine(entity));
    }
}
```

Sin esto, los ejercicios tendrían `routine_id = NULL` en la BD.

### El código generado completo (ExerciseMapperImpl.toEntity)

```java
public ExerciseEntity toEntity(ExerciseRequest request) {
    // 1. Crea el builder
    ExerciseEntity.ExerciseEntityBuilder builder = ExerciseEntity.builder();

    // 2. Mapea campos simples (por nombre)
    builder.name(request.name());
    builder.imageUrl(request.imageUrl());
    builder.orderInRoutine(request.orderInRoutine());
    builder.notes(request.notes());

    // 3. Convierte la lista de sets (delega a ExerciseSetMapper)
    builder.sets(exerciseSetRequestListToExerciseSetEntityList(request.sets()));

    // 4. Construye el objeto
    ExerciseEntity result = builder.build();

    // 5. DESPUÉS de construir: ejecuta @AfterMapping → enlaza sets con exercise
    setExerciseInSets(result);

    return result;
}
```

---

## Visualización del flujo completo

Cuando llega un JSON para crear una rutina:

```
POST /api/routines
{
  "name": "Push Day",
  "dayOfWeek": "MONDAY",
  "exercises": [
    {
      "name": "Press Banca",
      "orderInRoutine": 1,
      "sets": [
        { "setNumber": 1, "weightKg": 80, "reps": 10, "rpe": 7 },
        { "setNumber": 2, "weightKg": 85, "reps": 8, "rpe": 8 }
      ]
    }
  ]
}
```

El flujo de mapeo es:

```
RoutineRequest
  │
  ├─ RoutineMapper.toEntity()
  │    ├─ Mapea name, dayOfWeek, description
  │    ├─ Para cada ExerciseRequest → delega a ExerciseMapper.toEntity()
  │    │    ├─ Mapea name, imageUrl, orderInRoutine, notes
  │    │    ├─ Para cada ExerciseSetRequest → delega a ExerciseSetMapper.toEntity()
  │    │    │    └─ Mapea setNumber, weightKg, reps, rpe (ignora id y exercise)
  │    │    └─ @AfterMapping: set.setExercise(exercise) para cada set
  │    └─ @AfterMapping: exercise.setRoutine(routine) para cada exercise
  │
  └─ Resultado: RoutineEntity con todo el árbol enlazado, listo para save()
```

---

## Cómo ver el código generado

El código que MapStruct genera está en:
```
workout-service/target/generated-sources/annotations/com/fitness/workout/mapper/
├── ExerciseMapperImpl.java
├── ExerciseSetMapperImpl.java
└── RoutineMapperImpl.java
```

Puedes consultarlos en cualquier momento para entender exactamente qué está haciendo.
Se regeneran cada vez que compilas (`mvnw compile`).

## Integración con Lombok

MapStruct necesita el binding `lombok-mapstruct-binding` en el `pom.xml` para funcionar
con Lombok. Esto es porque ambos son **annotation processors** (se ejecutan en compilación)
y necesitan coordinarse:

- **Lombok** genera getters, setters y builders
- **MapStruct** los usa para crear el código de mapeo

Sin el binding, MapStruct podría ejecutarse antes que Lombok y no encontraría los getters/builders.
