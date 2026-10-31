Una pregu# Rutina Gym Tracker

## Qué es este proyecto
Proyecto personal de aprendizaje de microservicios con Spring Boot 4 + Angular.
Objetivo principal: aprender arquitectura de microservicios de forma práctica,
no shippear un producto. Prioriza claridad y buenas prácticas sobre velocidad.

## Stack
- Backend: Java 21, Spring Boot 4.1, Spring Data JPA, MySQL
- Mapeo: MapStruct 1.6.3 (con lombok-mapstruct-binding)
- Frontend: Angular (última versión, standalone components) — pendiente
- Infra: Docker Compose (local), Kafka (mensajería async entre servicios) — pendiente
- Testing: JUnit 5, Mockito, Testcontainers (MySQL)

## Arquitectura
- `workout-service`: gestión de rutinas, ejercicios y series de entrenamiento
- `analytics-service`: cálculo de progresión de fuerza (consume eventos de workouts vía Kafka) — pendiente
- `gateway`: API Gateway (Spring Cloud Gateway) — pendiente
- Cada microservicio tiene su propia base de datos (database per service)

### Modelo actual (workout-service)
- `ProgramEntity` → programa de entrenamiento (nombre, descripción, active). Solo puede haber uno activo a la vez.
- `RoutineEntity` → un día de rutina (nombre, día de la semana, descripción). Pertenece a un Program (nullable).
- `ExerciseTemplateEntity` → catálogo de ejercicios disponibles (nombre, grupo muscular, imageUrl, videoUrl). Independiente de cualquier rutina; se reutiliza en varias.
- `RoutineExerciseEntity` → asignación de un ejercicio del catálogo a una rutina concreta (orden, notas, lista de series). Es la entidad "puente" entre Routine y ExerciseTemplate.
- `ExerciseSetEntity` → serie individual (número, peso, repeticiones, RPE). Pertenece a un RoutineExercise.

Jerarquía completa:
```
Program 1──N Routine 1──N RoutineExercise ──→ ExerciseTemplate
                                    │
                                    └──N ExerciseSet
```
Todas las relaciones padre→hijo usan `cascade ALL, orphanRemoval`.

## Cómo trabajar en este proyecto (IMPORTANTE)

Este es un proyecto de aprendizaje, así que:

- **Comenta el código explicando el "por qué", no solo el "qué"**. Especialmente
  en anotaciones de Spring (@Transactional, @Service, etc.) explica brevemente
  qué hace y por qué se usa ahí.
- **Si usas un patrón o librería nueva** (Resilience4j, Testcontainers, Kafka
  producers/consumers, MapStruct...), añade un comentario corto con el concepto
  detrás, no solo el código.
- Sigue principios SOLID y arquitectura por capas estándar en Spring
  (controller → service → repository), sin atajos.
- Antes de implementar algo complejo (ej. comunicación entre microservicios,
  circuit breaker), explícame brevemente las opciones y el porqué de la elegida
  antes de escribir el código.
- Prefiero que me preguntes si algo es ambiguo antes de asumir — quiero
  entender las decisiones, no solo que "funcione".

## Convenciones de código
- Lombok para reducir boilerplate en entidades (@Builder, @Getter, @Setter...)
- DTOs como records de Java (inmutables, sin Lombok)
- DTOs separados de entidades JPA (nunca exponer entidades directamente en el API)
- MapStruct para mappers (interfaces con @Mapper, componentModel = "spring").
  Incluir siempre `toResponseList(List<Entity>)` para mapeo de listas — MapStruct
  reutiliza el `toResponse()` unitario internamente
- Services: interfaz + implementación (ej. RoutineService → RoutineServiceImpl)
- Nombres de tests: `should_doX_when_Y()`
- Commits en español, formato: `tipo: descripción breve` (feat, fix, refactor, docs)

## Seguridad (para aprender)
- Nunca escribas credenciales, contraseñas o secrets directamente en código o
  ficheros de configuración versionados. Usa siempre variables de entorno con ${VAR_NAME}

## Comandos (desde workout-service/)
- Levantar MySQL local: `docker compose up -d`
- Tests: `./mvnw test`
- Arrancar servicio: `DB_USERNAME=root DB_PASSWORD=admin ./mvnw spring-boot:run`
- Solo compilar (útil para verificar MapStruct): `./mvnw compile`

## Manejo de errores
- Excepciones custom en `workout-service/src/main/java/com/fitness/workout/exception/`:
  - `EntityNotFoundException` → entidad no encontrada (404)
  - `BusinessValidationException` → regla de negocio violada (400/422)
- `GlobalExceptionHandler` (`config/`) centraliza el mapeo de excepciones a respuestas HTTP con `ErrorResponse`.

## Documentación
- `docs/mapstruct.md`: guía de MapStruct y cómo funcionan los mappers del proyecto
- `docs/setup-local.md`: instrucciones de setup local
- `docs/exceptions.md`: manejo de excepciones y GlobalExceptionHandler
- `docs/spring-annotations.md`: referencia de anotaciones Spring usadas en el proyecto
- `docs/hibernate-jpa.md`: notas sobre JPA, dirty checking, fetch types y cascades
