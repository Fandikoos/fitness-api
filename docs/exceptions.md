# Excepciones en Java y Spring Boot

## 1. Qué es una excepción

Una excepción es un evento que interrumpe el flujo normal de ejecución de un programa.
En Java, toda excepción es un objeto que hereda de `Throwable`.

```
Throwable
├── Error                    → fallos graves de la JVM (OutOfMemoryError, StackOverflowError)
│                              No debes capturarlos ni lanzarlos tú mismo.
└── Exception
    ├── IOException          → checked: acceso a ficheros, red, BD
    ├── SQLException         → checked: operaciones SQL
    ├── RuntimeException     → unchecked: errores en tiempo de ejecución
    │   ├── NullPointerException
    │   ├── IllegalArgumentException
    │   ├── IllegalStateException
    │   └── ... (y las que tú crees)
    └── ...
```

---

## 2. Checked vs Unchecked: la diferencia clave

Esta es la distinción más importante en Java.

### Checked exceptions

- Heredan de `Exception` (pero NO de `RuntimeException`).
- El compilador te **obliga** a manejarlas: con `try/catch` o declarando `throws` en el método.
- Representan situaciones **externas y recuperables**: un fichero que no existe, un fallo de red.
- El objetivo es forzar al programador a pensar en qué hacer si algo externo falla.

```java
// El compilador no compila esto sin try/catch o throws IOException
public void leerFichero(String path) throws IOException {
    Files.readAllBytes(Path.of(path));
}
```

### Unchecked exceptions (RuntimeException)

- Heredan de `RuntimeException`.
- El compilador **no te obliga** a manejarlas ni declararlas.
- Representan errores de programación o violaciones de contrato que no deberían ocurrir
  si el código está bien escrito.
- Son las más usadas en aplicaciones modernas, especialmente en Spring.

```java
// Esto compila sin problema — nadie te obliga a capturarla
public RoutineResponse findById(Long id) {
    return routineRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rutina no encontrada"));
}
```

### ¿Por qué Spring usa casi solo unchecked?

Porque en una API REST, si algo falla (entidad no encontrada, validación de negocio),
no quieres capturar la excepción en el service y decidir qué HTTP status devolver ahí.
Quieres que la excepción suba sola hasta el `GlobalExceptionHandler`, que es el único
responsable de convertir errores en respuestas HTTP. Las checked exceptions romperían
ese flujo: te obligarían a capturarlas antes de que lleguen al handler.

---

## 3. Cuándo crear tus propias excepciones

Crea una excepción propia cuando quieras dar **semántica de dominio** a un error.
No uses excepciones genéricas de Java para errores de negocio — pierdes información.

```java
// Malo: usa una excepción genérica, sin contexto de dominio
throw new RuntimeException("Not found");

// Bueno: la excepción dice exactamente qué pasó en términos del dominio
throw new EntityNotFoundException("No se encontró la rutina con id: " + id);
```

### Regla práctica

| Situación | Qué lanzar |
|---|---|
| Entidad no encontrada en BD | `EntityNotFoundException` (propia, unchecked) |
| Violación de regla de negocio | `BusinessValidationException` (propia, unchecked) |
| Argumento inválido recibido internamente | `IllegalArgumentException` (Java estándar) |
| Estado inconsistente en el sistema | `IllegalStateException` (Java estándar) |
| Acceso a fichero/red | `IOException` (checked, de Java) |
| Error de integración con servicio externo | excepción propia o `RuntimeException` envuelta |

---

## 4. Cómo estructurar las excepciones en Spring Boot

### La excepción de dominio

```java
// Siempre extiende RuntimeException para que Spring pueda propagarla sin throws
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

### El handler centralizado: @RestControllerAdvice

`@RestControllerAdvice` intercepta todas las excepciones no capturadas que salgan
de cualquier `@RestController`. Sin él, Spring devolvería un error genérico 500.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Cada @ExceptionHandler gestiona un tipo concreto de excepción
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(EntityNotFoundException ex, WebRequest req) {
        // Aquí decides qué HTTP status, qué cuerpo de respuesta, etc.
        return new ResponseEntity<>(
            new ErrorResponse(404, "Not Found", ex.getMessage(), null,
                              LocalDateTime.now(), req.getDescription(false)),
            HttpStatus.NOT_FOUND
        );
    }
}
```

### El flujo completo

```
Request HTTP
    │
    ▼
Controller  (@Valid activa Bean Validation)
    │  MethodArgumentNotValidException si falla @NotBlank etc.
    │
    ▼
Service  (lanza EntityNotFoundException, BusinessValidationException...)
    │
    ▼
Repository
    │
    ◄── excepción sube sin capturarse
    │
    ▼
GlobalExceptionHandler  (@RestControllerAdvice)
    │
    ▼
Response HTTP con el status y body correctos
```

---

## 5. Códigos HTTP y qué excepción usar para cada uno

| HTTP Status | Cuándo usarlo | Excepción de dominio |
|---|---|---|
| 400 Bad Request | Formato del request inválido (campo vacío, tipo incorrecto) | `MethodArgumentNotValidException` (Spring) |
| 404 Not Found | Entidad no encontrada | `EntityNotFoundException` |
| 409 Conflict | Recurso ya existe (duplicado en BD) | `ResourceAlreadyExistsException` |
| 422 Unprocessable Entity | Request bien formado pero viola una regla de negocio | `BusinessValidationException` |
| 500 Internal Server Error | Error inesperado no controlado | cualquier excepción no manejada |

La diferencia entre **400 y 422** es importante:
- 400 → el cliente envió datos mal formados (campo requerido vacío, JSON inválido).
- 422 → los datos están bien formados pero la operación no tiene sentido en el dominio
  (ej. añadir el mismo ejercicio dos veces a una rutina).

---

## 6. Anti-patrones comunes

### Capturar y silenciar excepciones

```java
// MAL: tragar la excepción sin hacer nada oculta el error
try {
    routineRepository.save(routine);
} catch (Exception e) {
    // no hacer nada
}
```

### Capturar Exception o Throwable genéricamente

```java
// MAL: capturas más de lo que necesitas, incluyendo errores graves de la JVM
try {
    ...
} catch (Exception e) {
    log.error("Error", e);
}

// BIEN: captura solo lo que esperas y sabes manejar
try {
    ...
} catch (EntityNotFoundException e) {
    // manejo específico
}
```

### Usar excepciones para control de flujo

```java
// MAL: usar excepciones para "preguntar" si algo existe
try {
    findById(id);
    return true;
} catch (EntityNotFoundException e) {
    return false;
}

// BIEN: usa métodos de consulta explícitos
return routineRepository.existsById(id);
```

Las excepciones son costosas de crear (la JVM captura el stack trace completo).
Úsalas para situaciones excepcionales, no como `if/else`.

### Relanzar sin contexto

```java
// MAL: pierdes la causa original
try {
    ...
} catch (SQLException e) {
    throw new RuntimeException("Error");  // ¿qué error? ¿dónde?
}

// BIEN: pasa la causa original como segundo argumento
throw new RuntimeException("Error al guardar la rutina", e);
```

---

## 7. Cómo está implementado en este proyecto

```
exception/
├── EntityNotFoundException.java      → 404, entidad no encontrada en BD
└── BusinessValidationException.java  → 422, regla de negocio violada

dto/
└── ErrorResponse.java                → record con el cuerpo de error de la API

config/
└── GlobalExceptionHandler.java       → handlers para cada tipo de excepción
```

### EntityNotFoundException
Se lanza desde el service cuando `findById()` no encuentra nada en BD.
Mapea a **404 Not Found**.

```java
routineRepository.findById(id)
    .orElseThrow(() -> new EntityNotFoundException("No se encontró la rutina con id: " + id));
```

### BusinessValidationException
Se lanza desde el service cuando el request es válido técnicamente pero viola
una regla de dominio (ej. dos ejercicios iguales en la misma rutina).
Mapea a **422 Unprocessable Entity**.

```java
if (!templateIds.add(ex.exerciseTemplateId())) {
    throw new BusinessValidationException("No puede haber dos ejercicios iguales en la misma rutina");
}
```

### MethodArgumentNotValidException
La lanza Spring automáticamente cuando `@Valid` detecta que un DTO viola
sus anotaciones (`@NotBlank`, `@Min`, etc.). Mapea a **400 Bad Request**.
Nuestro handler extrae todos los errores de campo para devolverlos en la lista `errors`.
