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

## Modelo actual y sus mappers

El modelo del proyecto ha evolucionado. Ahora tenemos un árbol más complejo con
**ejercicios-plantilla** (catálogo reutilizable) separados de los **ejercicios dentro
de una rutina**:

```
ProgramEntity                    → programa de entrenamiento (ej. "PPL 4 días")
  └── RoutineEntity              → día de rutina (ej. "Push Day - Lunes")
        └── RoutineExerciseEntity → ejercicio asignado a esa rutina (orden, notas, series)
              ├── ExerciseTemplateEntity → plantilla del ejercicio (nombre, imagen, grupo muscular)
              └── ExerciseSetEntity      → serie individual (peso, reps, RPE)
```

Cada nivel tiene su propio mapper. La cadena completa de delegación es:

```
ProgramMapper (uses RoutineMapper)
    └── RoutineMapper (uses RoutineExerciseMapper)
            └── RoutineExerciseMapper (uses ExerciseTemplateMapper, ExerciseSetMapper)
                    ├── ExerciseTemplateMapper
                    └── ExerciseSetMapper
```

---

## Los 5 mappers del proyecto

### 1. ExerciseSetMapper (el más simple)

```java
@Mapper(componentModel = "spring")
public interface ExerciseSetMapper {

    ExerciseSetResponse toResponse(ExerciseSetEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "routineExercise", ignore = true)
    ExerciseSetEntity toEntity(ExerciseSetRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "routineExercise", ignore = true)
    void updateEntity(ExerciseSetRequest request, @MappingTarget ExerciseSetEntity entity);

    List<ExerciseSetResponse> toResponseList(List<ExerciseSetEntity> entities);
}
```

Campos ignorados:
- **id**: JPA lo genera con `@GeneratedValue`
- **routineExercise**: la relación padre se establece en el servicio, no en el mapper

El método `updateEntity()` usa `@MappingTarget` para **modificar un objeto existente**
en vez de crear uno nuevo. Esto es clave para updates: cargas la entidad de la BD y
actualizas sus campos, permitiendo que JPA haga dirty checking.

### 2. ExerciseTemplateMapper

```java
@Mapper(componentModel = "spring")
public interface ExerciseTemplateMapper {

    ExerciseTemplateResponse toResponse(ExerciseTemplateEntity entity);

    @Mapping(target = "id", ignore = true)
    ExerciseTemplateEntity toEntity(ExerciseTemplateRequest request);

    List<ExerciseTemplateResponse> toResponseList(List<ExerciseTemplateEntity> entities);
}
```

Es simple porque `ExerciseTemplateEntity` no tiene relaciones anidadas — es un catálogo
plano de ejercicios (nombre, grupo muscular, imagen, vídeo).

### 3. RoutineExerciseMapper

```java
@Mapper(componentModel = "spring", uses = {ExerciseTemplateMapper.class, ExerciseSetMapper.class})
public interface RoutineExerciseMapper {

    RoutineExerciseResponse toResponse(RoutineExerciseEntity entity);

    List<RoutineExerciseResponse> toResponseList(List<RoutineExerciseEntity> entities);
}
```

**No tiene `toEntity()`**. ¿Por qué? Porque crear un `RoutineExerciseEntity` requiere
buscar el `ExerciseTemplateEntity` por su ID en la base de datos. MapStruct no puede
hacer consultas a la BD, así que la creación se hace manualmente en el servicio.

Usa dos mappers delegados:
- `ExerciseTemplateMapper`: para convertir el template anidado dentro del response
- `ExerciseSetMapper`: para convertir la lista de series

### 4. RoutineMapper

```java
@Mapper(componentModel = "spring", uses = {RoutineExerciseMapper.class})
public interface RoutineMapper {

    @Mapping(source = "program.id", target = "programId")
    @Mapping(source = "routineExercises", target = "exercises")
    RoutineResponse toResponse(RoutineEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "routineExercises", ignore = true)
    RoutineEntity toEntity(RoutineRequest request);

    List<RoutineResponse> toResponseList(List<RoutineEntity> entities);
}
```

Este mapper tiene **dos `@Mapping` interesantes en `toResponse`**:

- `source = "program.id", target = "programId"`: extrae solo el ID del programa padre,
  en vez de incluir todo el objeto `ProgramEntity`. Así el DTO de respuesta tiene un
  `programId` (Long) en vez de un programa completo, evitando referencias circulares.
- `source = "routineExercises", target = "exercises"`: renombra el campo porque en la
  entidad se llama `routineExercises` pero en el DTO queremos exponerlo como `exercises`
  (más limpio para la API).

Campos ignorados en `toEntity()`:
- **program**: se establece en el servicio a partir del `programId` del request
- **routineExercises**: se gestionan por separado (necesitan buscar templates en la BD)

### 5. ProgramMapper

```java
@Mapper(componentModel = "spring", uses = {RoutineMapper.class})
public interface ProgramMapper {

    ProgramResponse toResponse(ProgramEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "routines", ignore = true)
    ProgramEntity toEntity(ProgramRequest request);

    List<ProgramResponse> toResponseList(List<ProgramEntity> entities);
}
```

Es el mapper de nivel más alto. Delega a `RoutineMapper` para convertir las rutinas
hijas, que a su vez delega a los mappers inferiores. Así se convierte el árbol completo
automáticamente.

Las rutinas se ignoran en `toEntity()` porque no se crean inline con el programa — se
gestionan por separado (un programa se crea primero, y luego se le añaden rutinas).

---

## Anotaciones clave

### `@Mapper(componentModel = "spring")`

Le dice a MapStruct que la clase generada sea un `@Component` de Spring.
Sin esto, MapStruct genera una clase normal y tendrías que instanciarla a mano.

### `@Mapping(target = "id", ignore = true)`

"No mapees este campo, déjalo en null." Lo usamos para:
- **id**: porque JPA lo genera automáticamente con `@GeneratedValue`
- **createdAt**: porque lo setea el `@PrePersist` de la entidad
- **relaciones padre** (program, routineExercise): porque se establecen manualmente en el servicio

### `@Mapping(source = "...", target = "...")`

Mapeo explícito cuando los nombres no coinciden entre origen y destino:

```java
// El campo se llama "routineExercises" en la entidad pero "exercises" en el DTO
@Mapping(source = "routineExercises", target = "exercises")

// Navega dentro de un objeto anidado para extraer solo un campo
@Mapping(source = "program.id", target = "programId")
```

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
@Mapper(componentModel = "spring", uses = {ExerciseTemplateMapper.class, ExerciseSetMapper.class})
public interface RoutineExerciseMapper { ... }
```

Esto genera un código donde `RoutineExerciseMapperImpl` inyecta ambos mappers y los usa:

```java
// Código generado: RoutineExerciseMapperImpl inyecta y usa los mappers delegados
@Autowired
private ExerciseTemplateMapper exerciseTemplateMapper;
@Autowired
private ExerciseSetMapper exerciseSetMapper;
```

---

## ¿Por qué algunos mappers no tienen `toEntity()`?

En la versión anterior del proyecto, todos los mappers tenían `toEntity()` y se usaba
`@AfterMapping` para establecer las relaciones bidireccionales (ej. `set.setExercise(exercise)`).

En el modelo actual, `RoutineExerciseMapper` **no tiene `toEntity()`** porque crear un
`RoutineExerciseEntity` requiere buscar el `ExerciseTemplateEntity` en la base de datos
por su ID. MapStruct solo sabe copiar campos — no puede hacer consultas a repositorios.

Por eso la creación se hace manualmente en el servicio:

```java
// En el servicio: se busca el template y se arma la entidad manualmente
ExerciseTemplateEntity template = exerciseTemplateRepository
    .findById(request.exerciseTemplateId())
    .orElseThrow(...);

RoutineExerciseEntity routineExercise = RoutineExerciseEntity.builder()
    .routine(routine)              // relación padre
    .exerciseTemplate(template)    // buscado en la BD
    .orderInRoutine(request.orderInRoutine())
    .notes(request.notes())
    .build();
```

Esto también elimina la necesidad de `@AfterMapping` — las relaciones bidireccionales
se establecen directamente al construir la entidad en el servicio.

---

## Visualización del flujo completo

### Crear un programa (solo mapea nombre y descripción)

```
POST /api/programs
{ "name": "Push Pull Legs", "description": "Rutina PPL de 6 días" }

ProgramRequest → ProgramMapper.toEntity() → ProgramEntity (sin rutinas, se añaden después)
```

### Obtener un programa con todo el árbol

```
GET /api/programs/1

ProgramEntity
  │
  ├─ ProgramMapper.toResponse()
  │    ├─ Mapea name, description, active, createdAt
  │    └─ Para cada RoutineEntity → delega a RoutineMapper.toResponse()
  │         ├─ Mapea name, dayOfWeek, description, createdAt
  │         ├─ Mapea program.id → programId
  │         └─ Para cada RoutineExerciseEntity → delega a RoutineExerciseMapper.toResponse()
  │              ├─ Mapea orderInRoutine, notes
  │              ├─ ExerciseTemplateEntity → delega a ExerciseTemplateMapper.toResponse()
  │              │    └─ Mapea name, muscleGroup, imageUrl, videoUrl
  │              └─ Para cada ExerciseSetEntity → delega a ExerciseSetMapper.toResponse()
  │                   └─ Mapea setNumber, weightKg, reps, rpe
  │
  └─ Resultado: ProgramResponse con todo el árbol convertido a DTOs
```

---

## Cómo ver el código generado

El código que MapStruct genera está en:
```
workout-service/target/generated-sources/annotations/com/fitness/workout/mapper/
├── ExerciseSetMapperImpl.java
├── ExerciseTemplateMapperImpl.java
├── ProgramMapperImpl.java
├── RoutineExerciseMapperImpl.java
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