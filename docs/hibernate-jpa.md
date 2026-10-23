# Hibernate y JPA — Guia practica

Conceptos clave de JPA e Hibernate que usamos en el proyecto, con buenas practicas
y errores comunes. Orientado a entender el "por que", no solo el "como".

---

## Indice

1. [JPA vs Hibernate — que es cada cosa](#jpa-vs-hibernate)
2. [Lazy vs Eager — estrategias de carga](#lazy-vs-eager)
3. [El problema N+1](#problema-n-plus-1)
4. [open-in-view — por que lo desactivamos](#open-in-view)
5. [Buenas practicas con entidades](#buenas-practicas-entidades)
6. [Errores comunes](#errores-comunes)

---

## JPA vs Hibernate — que es cada cosa {#jpa-vs-hibernate}

**JPA (Jakarta Persistence API)** es una **especificacion** — un conjunto de interfaces
y anotaciones que definen COMO deberia funcionar la persistencia en Java. No tiene codigo
ejecutable por si solo. Es como un contrato: "si quieres ser un ORM para Java, implementa
estas interfaces".

**Hibernate** es una **implementacion** de esa especificacion. Es la libreria que realmente
ejecuta el codigo, genera las queries SQL, gestiona la cache, etc.

```
JPA (especificacion)          Hibernate (implementacion)
---------------------         -------------------------
@Entity                  -->  Hibernate lee esta anotacion y mapea la clase a una tabla
EntityManager            -->  Hibernate provee la implementacion real
@OneToMany               -->  Hibernate genera los JOINs necesarios
```

**Analogia:** JPA es como la interfaz `List` en Java — define que metodos debe tener una
lista. Hibernate es como `ArrayList` — la implementacion concreta que usas.

**Por que importa esta distincion:** Si miras los imports de tus entidades, veras que son
de `jakarta.persistence.*` (JPA), no de `org.hibernate.*`. Esto significa que tu codigo
depende de la especificacion, no de la implementacion. En teoria podrias cambiar Hibernate
por otra implementacion (EclipseLink, por ejemplo) sin tocar tus entidades.

En la practica, casi todo el mundo usa Hibernate y rara vez se cambia, pero es buena
practica depender de las interfaces JPA.

---

## Lazy vs Eager — estrategias de carga {#lazy-vs-eager}

Cuando una entidad tiene relaciones (@OneToMany, @ManyToOne, etc.), Hibernate necesita
saber CUANDO cargar los datos relacionados. Tiene dos estrategias:

### FetchType.EAGER — "carga todo de golpe"

```java
@OneToMany(fetch = FetchType.EAGER)
private List<ExerciseEntity> exercises;
```

Cuando cargas una Routine, Hibernate SIEMPRE carga tambien todos sus Exercises,
aunque no los necesites. Hace un JOIN o queries adicionales automaticamente.

### FetchType.LAZY — "carga solo cuando lo pidas"

```java
@OneToMany(fetch = FetchType.LAZY)
private List<ExerciseEntity> exercises;
```

Cuando cargas una Routine, Hibernate NO carga los Exercises. Solo los carga cuando
accedes a `routine.getExercises()` por primera vez. Mientras tanto, ese campo contiene
un "proxy" — un objeto falso que Hibernate usa como placeholder.

### Por que LAZY es casi siempre mejor

Imagina que tienes un endpoint `GET /routines` que lista todas las rutinas.
Solo necesitas nombre y dia de la semana — no necesitas los ejercicios.

- **Con EAGER:** Hibernate carga TODAS las rutinas CON todos sus ejercicios CON
  todas sus series. Toneladas de datos que no vas a usar. Queries innecesarias.
- **Con LAZY:** Hibernate solo carga las rutinas. Rapido y ligero.

### Valores por defecto de JPA

| Relacion     | Default  | Por que |
|-------------|----------|---------|
| @OneToMany  | LAZY     | Una entidad puede tener muchos hijos — cargarlos siempre seria peligroso |
| @ManyToMany | LAZY     | Misma razon |
| @ManyToOne  | EAGER    | Es solo un objeto, el "coste" es bajo |
| @OneToOne   | EAGER    | Misma razon |

**Recomendacion:** Pon LAZY en todas las relaciones, incluyendo @ManyToOne y @OneToOne.
Es mas facil cargar datos cuando los necesitas (con JOIN FETCH) que intentar evitar
cargas innecesarias con EAGER.

```java
// Recomendado: forzar LAZY incluso en @ManyToOne
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "routine_id")
private RoutineEntity routine;
```

### Cuando EAGER tiene sentido

Casi nunca en la practica. Quizas si SIEMPRE necesitas el dato relacionado en TODOS
los casos de uso, pero incluso asi, un JOIN FETCH explicito es mas claro y controlable.

---

## El problema N+1 {#problema-n-plus-1}

Es el error de rendimiento mas comun con Hibernate. Ocurre con relaciones LAZY cuando
accedes a los datos relacionados dentro de un bucle.

### Ejemplo del problema

```java
// 1 query: SELECT * FROM routines
List<RoutineEntity> routines = routineRepository.findAll();

// N queries: por cada rutina, SELECT * FROM exercises WHERE routine_id = ?
for (RoutineEntity routine : routines) {
    System.out.println(routine.getExercises().size()); // Aqui dispara la query
}
```

Si tienes 50 rutinas, esto genera **51 queries** (1 para rutinas + 50 para ejercicios).
De ahi el nombre "N+1".

### Como solucionarlo: JOIN FETCH

Usas una query JPQL con `JOIN FETCH` para traer todo en una sola query:

```java
// En tu repositorio:
@Query("SELECT r FROM RoutineEntity r JOIN FETCH r.exercises")
List<RoutineEntity> findAllWithExercises();
```

Esto genera UN solo `SELECT ... JOIN` que trae rutinas y ejercicios de golpe.

### Como detectarlo

Activa el logging de queries SQL en tu `application.properties`:

```properties
# Muestra las queries SQL que genera Hibernate (solo para desarrollo)
spring.jpa.show-sql=true

# Formato mas legible
spring.jpa.properties.hibernate.format_sql=true
```

Si ves muchas queries repetidas con el mismo patron (SELECT de la misma tabla
con distinto WHERE), probablemente tienes un N+1.

**Importante:** Desactiva `show-sql` en produccion — genera muchos logs y afecta
al rendimiento.

---

## open-in-view — por que lo desactivamos {#open-in-view}

Spring Boot tiene `spring.jpa.open-in-view=true` por defecto. Esto mantiene la
sesion de Hibernate abierta durante TODA la peticion HTTP, incluyendo la fase de
serializacion a JSON.

### Por que es problematico

```
Peticion HTTP
  |
  +-- Controller (sesion abierta)
  |     |
  |     +-- Service (transaccion activa, aqui deberia estar la logica)
  |     |
  |     +-- Serializacion a JSON (sesion abierta, SIN transaccion)
  |           |
  |           +-- Jackson accede a routine.getExercises()
  |           +-- Hibernate dispara una query AQUI, fuera del service
  |           +-- Esto funciona pero es impredecible y dificil de debuggear
  |
  Respuesta
```

El problema es que las queries se disparan en sitios inesperados (durante la
serializacion JSON), fuera de tu control. Si algo falla, los errores son confusos.

### Nuestra configuracion

```properties
# Desactivado: las queries solo ocurren dentro de @Transactional (en el service)
spring.jpa.open-in-view=false
```

Con open-in-view desactivado, si intentas acceder a una relacion LAZY fuera del
service (por ejemplo, en el controller o durante la serializacion), obtienes un
`LazyInitializationException`. Esto es BUENO — te obliga a cargar los datos
explicitamente en el service, donde tienes control.

La solucion es siempre usar DTOs (como ya hacemos con MapStruct), que convierten
la entidad a un objeto plano DENTRO del service, antes de salir de la transaccion.

---

## Buenas practicas con entidades {#buenas-practicas-entidades}

### 1. Nunca exponer entidades en el API

```java
// MAL: el controller devuelve la entidad directamente
@GetMapping("/{id}")
public RoutineEntity getById(@PathVariable Long id) { ... }

// BIEN: el controller devuelve un DTO
@GetMapping("/{id}")
public RoutineResponse getById(@PathVariable Long id) { ... }
```

Razones:
- **Seguridad:** puedes exponer campos que no deberias (passwords, ids internos)
- **Acoplamiento:** si cambias la entidad, cambias el API automaticamente
- **Control:** con DTOs eliges exactamente que datos devolver

Nosotros ya lo hacemos con records de Java + MapStruct.

### 2. Equals y hashCode con entidades

Lombok genera `equals()` y `hashCode()` usando todos los campos por defecto.
Con entidades JPA esto puede dar problemas porque el `id` es `null` antes del
primer `save()`.

```java
// CUIDADO con @Data o @EqualsAndHashCode en entidades
// Mejor usar solo @Getter y @Setter, y si necesitas equals/hashCode,
// basarlo en una clave de negocio o solo en el id (con cuidado)
@Getter
@Setter
@Entity
public class RoutineEntity {
    // ...
}
```

### 3. Cascade con cuidado

`CascadeType.ALL` es comodo pero puede ser peligroso si no se entiende bien:

```java
// CascadeType.ALL = las operaciones sobre Routine se propagan a Exercise
// Si guardas una Routine, tambien guarda sus Exercises (bien)
// Si BORRAS una Routine, tambien BORRA todos sus Exercises (puede ser lo que quieres o no)
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
private List<ExerciseEntity> exercises;
```

- **cascade** propaga operaciones (save, delete, etc.) del padre al hijo
- **orphanRemoval** elimina hijos que se quitan de la lista del padre

En nuestro proyecto lo usamos en Routine -> Exercise -> ExerciseSet porque tiene
sentido: si borras una rutina, borras sus ejercicios. Pero no pongas
`CascadeType.ALL` en todas las relaciones por defecto — piensa si realmente
quieres propagar borrados.

### 4. toString y relaciones

Cuidado con `@ToString` de Lombok en entidades con relaciones:

```java
// MAL: si RoutineEntity.toString() incluye exercises,
// y ExerciseEntity.toString() incluye routine,
// tienes un bucle infinito -> StackOverflowError
@ToString
@Entity
public class RoutineEntity {
    @OneToMany
    private List<ExerciseEntity> exercises; // toString llama a exercise.toString()...
}
```

Solucion: excluir las relaciones del toString.

```java
@ToString(exclude = "exercises")
```

---

## Errores comunes {#errores-comunes}

### LazyInitializationException

```
org.hibernate.LazyInitializationException: could not initialize proxy - no Session
```

**Que significa:** Intentaste acceder a una relacion LAZY fuera de una transaccion
(la sesion de Hibernate ya se cerro).

**Solucion:** No reabrir la sesion (no uses open-in-view como parche). En su lugar,
carga los datos que necesitas DENTRO del service con JOIN FETCH o inicializando
la coleccion antes de devolver el DTO.

### MultipleBagFetchException

```
org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
```

**Que significa:** Intentaste hacer JOIN FETCH de dos colecciones `List` a la vez.
Hibernate no puede hacer dos JOINs a listas simultaneamente porque genera un
producto cartesiano.

**Solucion:** Usa `Set` en vez de `List` para las colecciones, o haz los
JOIN FETCH en queries separadas.

### Detached entity passed to persist

```
org.hibernate.PersistentObjectException: detached entity passed to persist
```

**Que significa:** Estas intentando guardar una entidad que ya tiene un id asignado
pero no esta asociada a la sesion actual de Hibernate. Hibernate no sabe si es
nueva o existente.

**Solucion:** Usa `merge()` en vez de `persist()`, o asegurate de que la entidad
viene de un `findById()` dentro de la misma transaccion.

---

## Resumen rapido

| Concepto | Recomendacion |
|----------|--------------|
| Fetch type | LAZY siempre, JOIN FETCH cuando necesites los datos |
| open-in-view | Desactivado (spring.jpa.open-in-view=false) |
| Entidades en el API | Nunca, siempre usar DTOs |
| CascadeType.ALL | Solo cuando el hijo no tiene sentido sin el padre |
| N+1 | Detectar con show-sql, solucionar con JOIN FETCH |
| @ToString en entidades | Excluir relaciones para evitar bucles |
