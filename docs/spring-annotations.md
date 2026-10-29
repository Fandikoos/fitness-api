# Anotaciones de Spring Boot — Guía práctica

> **Prerequisito:** Lee primero [`spring-ioc-container.md`](./spring-ioc-container.md).
> Explica qué es el contenedor, los beans y los singletons. Sin ese contexto,
> las anotaciones son solo "magia" sin entender por qué existen.

---

## Índice

1. [Estereotipos: @Component, @Service, @Repository, @Controller](#estereotipos)
2. [@Configuration y @Bean — cuándo usar cada uno](#configuration-bean)
3. [@RestController y @RequestMapping](#rest)
4. [@Transactional](#transactional)
5. [@ExceptionHandler y @RestControllerAdvice](#exception-handler)

---

## Estereotipos: @Component, @Service, @Repository, @Controller {#estereotipos}

Estas cuatro anotaciones hacen lo mismo a nivel técnico: le dicen a Spring
"escaneame, crea una instancia mía y guárdala en el contenedor".

```
@Component        ← genérica: "esto es un bean de Spring"
   │
   ├── @Service        ← "esto es lógica de negocio"
   ├── @Repository     ← "esto accede a datos"
   └── @Controller     ← "esto maneja peticiones HTTP"
```

La diferencia **no es técnica, es semántica**, con una excepción:

| Anotación      | Comportamiento extra                                             |
|----------------|------------------------------------------------------------------|
| `@Component`   | Ninguno. Bean genérico.                                          |
| `@Service`     | Ninguno. Solo indica que es lógica de negocio (documentación).  |
| `@Repository`  | Traduce excepciones de BD (SQLException, etc.) a excepciones de Spring (`DataAccessException`). |
| `@Controller`  | Habilita el manejo de peticiones HTTP y resolución de vistas.    |

### Por qué hay cuatro si hacen lo mismo

Para que el código sea **autodocumentado**. Al leer la anotación ya sabes la
responsabilidad de la clase sin leer su implementación.

```java
// Sin leer nada más, ya sabes qué hace cada clase:
@Repository  → accede a datos
@Service     → lógica de negocio
@Controller  → maneja peticiones HTTP
@Component   → utilidad/helper sin categoría clara
```

### Cuándo usar @Component

Para clases que no encajan en las otras tres categorías: validadores, utilidades,
convertidores, etc. En nuestro proyecto:

```java
// RoutineValidator no es un Service (no orquesta flujo completo),
// no es un Repository (no accede a BD directamente), no es un Controller.
// Es una utilidad de dominio → @Component es la elección correcta.
@Component
public class RoutineValidator {
    private final RoutineRepository routineRepository;

    public RoutineValidator(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }
    // ...
}
```

### El component scanning: cómo Spring encuentra tus clases

Spring escanea automáticamente todos los paquetes **a partir del paquete donde
está `@SpringBootApplication`**. En este proyecto:

```
com.fitness.workout          ← @SpringBootApplication está aquí
    ├── service/             ← escaneado ✓
    ├── repository/          ← escaneado ✓
    ├── controller/          ← escaneado ✓
    ├── validation/          ← escaneado ✓
    └── config/              ← escaneado ✓
```

Si pusieras una clase con `@Component` fuera de este árbol de paquetes,
Spring no la encontraría y tendrías un `NoSuchBeanDefinitionException`.

---

## @Configuration y @Bean — cuándo usar cada uno {#configuration-bean}

Hay dos formas de registrar beans. Confundirlas es uno de los errores más frecuentes.

### @Component (y variantes): para TUS clases

```java
// ✅ Clase que tú escribes → puedes anotarla directamente
@Service
public class RoutineServiceImpl implements RoutineService { ... }
```

### @Bean: para objetos externos o con configuración especial

`@Bean` se usa **dentro de una clase `@Configuration`** para crear objetos que:
- Pertenecen a una librería externa (no puedes añadirles `@Component`)
- Necesitan configuración especial al construirse

```java
@Configuration
public class JacksonConfig {

    // Jackson es una librería externa → no puedes ponerle @Component a ObjectMapper
    // Pero quieres que Spring lo gestione → @Bean
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configuración especial que no puedes hacer con @Component
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
```

### La tabla de decisión

```
¿Es una clase que tú escribes?
  → @Component / @Service / @Repository / @Controller

¿Es una clase de una librería (Jackson, HttpClient, etc.)?
  → @Bean dentro de @Configuration

¿Es tu clase pero necesita configuración compleja al crearse?
  → Puedes usar @Bean (más control) o @Component con @Value/@PostConstruct
```

### Por qué @Configuration no es lo mismo que @Component

`@Configuration` tiene un comportamiento especial: Spring crea un **proxy** de la clase.
Esto importa cuando tienes métodos `@Bean` que se llaman entre sí:

```java
@Configuration
public class DataConfig {

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(/* configuración */);
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        // Llama a dataSource() — con @Configuration, Spring intercepta esta llamada
        // y devuelve el bean ya creado (singleton). No crea uno nuevo.
        return factory(dataSource());
    }

    @Bean
    public TransactionManager transactionManager() {
        // También llama a dataSource() — sigue siendo el mismo objeto
        return new JpaTransactionManager(dataSource());
    }
}
```

**Con `@Configuration`:** las tres llaman a `dataSource()` → todas reciben
**la misma instancia** → un solo pool de conexiones. ✅

**Si pusieras solo `@Component`:** no hay proxy → cada llamada a `dataSource()`
ejecuta el método de verdad → crea un objeto nuevo → tres pools distintos. ❌

**Regla práctica:** Si tu clase `@Configuration` tiene métodos `@Bean` que se llaman
entre sí, el proxy importa. Si no, la diferencia es irrelevante para el resultado.

---

## @RestController y @RequestMapping {#rest}

### @RestController

Es una combinación de dos anotaciones:

```
@RestController = @Controller + @ResponseBody
```

- `@Controller`: registra la clase como bean que maneja peticiones HTTP
- `@ResponseBody`: indica que el valor de retorno de los métodos se serializa a JSON
  directamente (no busca una vista/template HTML)

```java
@RestController  // Cada método devuelve JSON automáticamente
@RequestMapping("/api/workout-service/routines")
public class RoutineController {

    @GetMapping         // GET /api/workout-service/routines
    public List<RoutineResponse> getAll() { ... }

    @GetMapping("/{id}")  // GET /api/workout-service/routines/123
    public RoutineResponse getById(@PathVariable Long id) { ... }

    @PostMapping        // POST /api/workout-service/routines
    @ResponseStatus(HttpStatus.CREATED)  // Devuelve 201 en vez de 200
    public RoutineResponse create(@RequestBody RoutineRequest request) { ... }
}
```

### @RequestMapping y las variantes HTTP

```java
@GetMapping("/ruta")     // = @RequestMapping(method = GET)
@PostMapping("/ruta")    // = @RequestMapping(method = POST)
@PutMapping("/ruta")     // = @RequestMapping(method = PUT)
@DeleteMapping("/ruta")  // = @RequestMapping(method = DELETE)
@PatchMapping("/ruta")   // = @RequestMapping(method = PATCH)
```

Las variantes (`@GetMapping`, etc.) son simplificaciones de `@RequestMapping`.
Usa siempre las variantes: son más legibles y explícitas.

---

## @Transactional {#transactional}

### Qué hace

Envuelve la ejecución de un método dentro de una transacción de base de datos:
- Si todo va bien → `COMMIT` (los cambios se persisten)
- Si se lanza una excepción → `ROLLBACK` automático (la BD queda como estaba)

```java
@Service
@Transactional  // Todos los métodos públicos son transaccionales por defecto
public class RoutineServiceImpl implements RoutineService {

    @Override
    public RoutineResponse create(RoutineRequest request) {
        // Si esto falla a mitad, el rollback deshace TODO lo que se haya hecho
        RoutineEntity saved = routineRepository.save(routine);
        return routineMapper.toResponse(saved);
    }

    // Este método sobreescribe el @Transactional de la clase con readOnly = true
    @Override
    @Transactional(readOnly = true)
    public RoutineResponse findById(Long id) {
        return routineMapper.toResponse(routineRepository.findById(id)...);
    }
}
```

### Por qué importa: el ejemplo con cascadas

En nuestro modelo, al guardar una `RoutineEntity` con `cascade ALL`, JPA también
intenta guardar todos sus `RoutineExerciseEntity` hijos. Sin transacción:

```
1. INSERT routine       ✓
2. INSERT exercise_1    ✓
3. INSERT exercise_2    ✗ (falla por constraint)
```

Resultado sin `@Transactional`: la rutina existe en BD pero sin todos sus ejercicios.
Estado inconsistente.

Con `@Transactional`: el fallo en el paso 3 hace rollback de los pasos 1 y 2.
O todo o nada.

### readOnly = true — optimización para lecturas

```java
@Transactional(readOnly = true)
public List<RoutineResponse> findAll() { ... }
```

Le dice a Hibernate: "este método solo lee, no modifica". Beneficios:
1. **Desactiva el dirty checking**: Hibernate normalmente compara todos los objetos
   cargados con su estado original al final de la transacción (para detectar cambios).
   Con `readOnly`, se salta esa comparación → menos CPU.
2. **Documenta la intención**: al leer el código sabes que ese método no modifica datos.

### El error clásico: llamar @Transactional desde la misma clase

```java
@Transactional
public class MiServicio {

    public void metodoA() {
        metodoB();  // ← NO abre nueva transacción, aunque metodoB tenga @Transactional
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void metodoB() { ... }
}
```

Spring aplica `@Transactional` mediante un **proxy** que envuelve el bean.
Las peticiones externas pasan por el proxy. Las llamadas internas (`this.metodoB()`)
se saltan el proxy → el `@Transactional` de `metodoB` no tiene efecto.

**Solución**: extraer `metodoB` a otro bean con su propio `@Transactional`.

### Cuándo hace rollback

Por defecto, `@Transactional` solo hace rollback con **`RuntimeException`** y **`Error`**.
Las `checked exceptions` (que el compilador te obliga a declarar) NO hacen rollback por defecto.

```java
// ✅ Hace rollback automáticamente (son RuntimeException):
throw new EntityNotFoundException(...);
throw new BusinessValidationException(...);
throw new ConflictException(...);

// ❌ NO hace rollback por defecto (es checked exception):
throw new IOException(...);

// Para forzarlo:
@Transactional(rollbackFor = IOException.class)
```

En este proyecto, todas nuestras excepciones custom extienden `RuntimeException`,
así que el comportamiento por defecto es el correcto.

---

## @ExceptionHandler y @RestControllerAdvice {#exception-handler}

### El problema que resuelven

Sin manejo centralizado de errores, cada controller tendría que capturar
sus propias excepciones con try-catch y devolver el error en el formato correcto.
Repetitivo y fácil de inconsistente.

### @RestControllerAdvice: el interceptor global

```java
// @RestControllerAdvice intercepta excepciones lanzadas en CUALQUIER
// @RestController de la aplicación. Es un único punto para toda la gestión de errores.
@RestControllerAdvice
public class GlobalExceptionHandler {
    ...
}
```

Técnicamente es `@ControllerAdvice` + `@ResponseBody` (igual que `@RestController`).

### @ExceptionHandler: asociar una excepción a un método

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Cuando CUALQUIER controller lance EntityNotFoundException,
    // Spring ejecuta este método en lugar de devolver un 500 genérico.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),  // 404
                "Not Found",
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                request.getDescription(false)  // "uri=/api/routines/5"
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```

### El flujo completo de una excepción

```
RoutineController.create()
    → RoutineServiceImpl.create()
        → RoutineValidator.validateForCreate()
            → lanza ConflictException("Ya existe rutina para MONDAY...")
        ← ConflictException sube por la pila de llamadas
    ← ConflictException sale del servicio
← ConflictException sale del controller

Spring intercepta la excepción (no llega al cliente como error 500)
    ↓
GlobalExceptionHandler.handleConflict() ← el @ExceptionHandler correcto
    ↓
Devuelve ResponseEntity con status 409 y cuerpo ErrorResponse
    ↓
Cliente recibe: HTTP 409 + JSON { "status": 409, "error": "Conflict", ... }
```

### Mapeo de excepciones a códigos HTTP en este proyecto

| Excepción                        | HTTP | Cuándo                                         |
|----------------------------------|------|------------------------------------------------|
| `EntityNotFoundException`        | 404  | Recurso no encontrado (rutina, ejercicio, etc) |
| `BusinessValidationException`    | 422  | Regla de negocio violada dentro del request    |
| `ConflictException`              | 409  | Choque con estado existente en la BD           |
| `DataIntegrityViolationException`| 409  | Constraint de BD violada (red de seguridad)    |
| `MethodArgumentNotValidException`| 400  | Bean Validation fallida (@NotNull, @Size, etc) |

---

## Próximas anotaciones (se añadirán cuando se usen en el proyecto)

- `@Valid` / `@Validated` — activar Bean Validation en controllers
- `@Value` — inyectar propiedades de `application.properties` en beans
- `@EnableKafka` / `@KafkaListener` — cuando se integre Kafka
- `@CircuitBreaker` (Resilience4j) — cuando se implemente tolerancia a fallos
