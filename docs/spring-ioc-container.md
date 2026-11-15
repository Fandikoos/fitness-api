# Cómo funciona Spring por dentro: el Contenedor IoC

> Lee esto ANTES que el resto de docs de Spring. Todo lo demás (anotaciones,
> beans, singletons...) tiene mucho más sentido una vez entiendes este concepto base.

---

## El problema que Spring resuelve

Imagina que tienes estas clases en tu proyecto:

```java
public class RoutineServiceImpl {
    private final RoutineRepository routineRepository;
    private final RoutineMapper routineMapper;
    private final RoutineValidator routineValidator;

    // Para crear un RoutineServiceImpl necesito crear sus 3 dependencias antes
    public RoutineServiceImpl(...) { ... }
}
```

Sin Spring, para usar `RoutineServiceImpl` en tu controller tendrías que hacer esto:

```java
// En el controller, o en algún sitio al arrancar la app...
RoutineRepository repo = new RoutineRepository(dataSource);
RoutineMapper mapper = new RoutineMapper();
RoutineValidator validator = new RoutineValidator(repo);  // también necesita repo!
RoutineServiceImpl service = new RoutineServiceImpl(repo, mapper, validator);
```

Esto tiene varios problemas:
1. **Acoplamiento**: el controller sabe CÓMO construir el servicio (sus dependencias internas)
2. **Duplicación**: si 3 sitios necesitan `RoutineServiceImpl`, creas 3 instancias distintas
3. **Escalabilidad**: en una app real con 50 clases, gestionar esto a mano es inmanejable

**Spring resuelve esto**: tú declaras qué objetos existen y qué necesitan, y Spring se
encarga de crearlos, ensamblarlos e inyectarlos donde hagan falta. Esto se llama
**Inversión de Control** (IoC) y **Inyección de Dependencias** (DI).

---

## El Contenedor: la caja de Spring

Piensa en Spring como una **caja**. Al arrancar la aplicación, Spring:

```
ARRANQUE DE LA APP
        │
        ▼
┌─────────────────────────────────────────────┐
│  Spring escanea todas las clases del        │
│  proyecto buscando anotaciones              │
│  (@Component, @Service, @Repository...)     │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  Para cada clase encontrada:                │
│  1. Lee qué dependencias necesita           │
│     (parámetros del constructor)            │
│  2. Crea las dependencias primero           │
│  3. Crea la clase inyectándoselas           │
│  4. Guarda la instancia en la "caja"        │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  ApplicationContext (la "caja")             │
│  ┌────────────────────────────────────┐     │
│  │ "routineServiceImpl" → instancia   │     │
│  │ "routineValidator"   → instancia   │     │
│  │ "routineRepository"  → instancia   │     │
│  │ "routineMapper"      → instancia   │     │
│  │ ... (todos los beans)              │     │
│  └────────────────────────────────────┘     │
└─────────────────────────────────────────────┘
                  │
                  ▼
        App lista para recibir peticiones
```

Este "ApplicationContext" es lo que se llama el **contenedor IoC**.

---

## Qué es un Bean

**Un bean es simplemente un objeto que Spring gestiona.**

La diferencia entre un objeto normal y un bean:

| Objeto normal                       | Bean de Spring                          |
|-------------------------------------|-----------------------------------------|
| Tú lo creas con `new`               | Spring lo crea al arrancar              |
| Tú gestionas su ciclo de vida       | Spring lo gestiona                      |
| Tú lo pasas a quien lo necesita     | Spring lo inyecta automáticamente       |
| Puede haber N instancias            | Por defecto, solo hay UNA (singleton)   |

Para que Spring sepa que algo debe ser un bean, necesita una señal:
- En una clase tuya → `@Component`, `@Service`, `@Repository`, `@Controller`
- Para un objeto externo o con configuración especial → `@Bean` dentro de `@Configuration`

---

## Singleton: una sola instancia para toda la app

Este es uno de los conceptos que más confunde. Por defecto, Spring crea
**una sola instancia** de cada bean y la reutiliza en todos los sitios donde se necesita.

```
RoutineValidator (bean singleton)
        │
        ├──── inyectado en RoutineServiceImpl (instancia única)
        │
        └──── si también lo usara ProgramServiceImpl (misma instancia)
```

### ¿Por qué una sola instancia?

Porque los beans en Spring son **sin estado** (stateless): no guardan datos específicos
de una petición en sus campos. Solo tienen lógica y referencias a otros beans.

```java
@Component
public class RoutineValidator {
    // ✅ BIEN: dependencia estática, siempre la misma
    private final RoutineRepository routineRepository;

    public void validateForCreate(RoutineRequest request) {
        // La lógica usa parámetros del método, no campos de la instancia
        // → es seguro compartir esta instancia entre peticiones
    }
}
```

Si un bean guardara estado en un campo, el singleton sería problemático:

```java
@Component
public class MalEjemplo {
    // ❌ MAL: campo mutable. Si petición A escribe aquí y petición B lee,
    // petición B lee datos de petición A → bug de concurrencia
    private String usuarioActual;
}
```

### Analogía

Piensa en un bean singleton como una **máquina de café en una oficina**:
- Hay una sola máquina (una instancia)
- Todos los empleados (controllers, services) la usan
- Cada uno la usa con sus propios ingredientes (parámetros del método)
- La máquina en sí no "recuerda" quién la usó antes

---

## Cómo Spring resuelve las dependencias al arrancar

Veamos paso a paso qué hace Spring con nuestro proyecto real:

**Orden de creación (Spring lo calcula solo):**

```
1. RoutineRepository       ← no tiene dependencias, se crea primero
2. RoutineMapper           ← no tiene dependencias
3. ExerciseSetMapper       ← no tiene dependencias
4. RoutineValidator        ← necesita RoutineRepository → ya existe en paso 1 ✓
5. RoutineServiceImpl      ← necesita Repository, Mapper, Validator → ya existen ✓
6. RoutineController       ← necesita RoutineService → ya existe en paso 5 ✓
```

Spring construye un **grafo de dependencias** y los crea en el orden correcto.
Si hubiera una dependencia circular (A necesita B y B necesita A), Spring lanzaría
un error al arrancar, no en tiempo de ejecución.

---

## La diferencia entre @Component y @Bean

Aquí está la fuente de confusión más frecuente. Son dos formas distintas de
registrar un bean, para dos situaciones distintas.

### @Component: "esta clase MÍA es un bean"

```java
// Clase que TÚ escribes → puedes añadir anotaciones
@Component  // Spring: "escanéame, crea una instancia mía y guárdala"
public class RoutineValidator {
    ...
}
```

Spring lo descubre automáticamente escaneando el classpath. Es la forma más simple.

### @Bean: "yo controlo cómo se crea este objeto"

```java
@Configuration
public class MiConfig {

    // Este método produce un bean. Spring lo llama al arrancar y guarda el resultado.
    @Bean
    public ObjectMapper objectMapper() {
        // ObjectMapper es de la librería Jackson → no puedes ponerle @Component
        // porque no es tu clase. Pero quieres que Spring la gestione.
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;  // ← Spring guarda este objeto como bean
    }
}
```

**Regla práctica:**

```
¿Es una clase que tú escribes?
  → @Component / @Service / @Repository

¿Es una clase de una librería externa?
  → @Bean dentro de @Configuration

¿Necesita configuración especial al crearse (parámetros, flags, etc.)?
  → @Bean dentro de @Configuration
```

---

## @Configuration: por qué no basta con @Component

Podría parecer que `@Configuration` es redundante (ya sabemos que también es
un `@Component`). La diferencia está en un detalle técnico importante: el **proxy**.

```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(...);
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        // Llama al método dataSource() de arriba
        return new LocalContainerEntityManagerFactoryBean(dataSource(), ...);
    }

    @Bean
    public TransactionManager transactionManager() {
        // También llama a dataSource()
        return new JpaTransactionManager(dataSource(), ...);
    }
}
```

**Con `@Configuration`:** Spring crea un proxy de esta clase. Las llamadas a
`dataSource()` dentro de la clase son interceptadas por el proxy, que devuelve
**siempre la misma instancia** (la que ya está en el contenedor). Un solo DataSource.

**Sin `@Configuration` (solo `@Component`):** No hay proxy. Cada llamada a
`dataSource()` ejecuta el método de verdad → crea un objeto nuevo cada vez →
tendrías 3 conexiones a la BD distintas. Bug difícil de detectar.

**Regla:** Si tu clase tiene métodos `@Bean` que se llaman entre sí, usa
`@Configuration`. Si no, la diferencia es irrelevante.

---

## Inyección por constructor: la forma recomendada

Hay tres formas de inyectar dependencias en Spring. Solo deberías usar una.

### ❌ Inyección por campo (no uses esto)

```java
@Service
public class RoutineServiceImpl {
    @Autowired  // Spring inyecta por reflexión, saltándose el constructor
    private RoutineRepository routineRepository;
}
```

**Por qué no:** No puedes crear la clase en un test sin Spring. Los campos son
`private`, así que en tu test `RoutineServiceImpl service = new RoutineServiceImpl()`
crea un objeto con `routineRepository = null` → NullPointerException.

### ❌ Inyección por setter (casi nunca)

```java
@Service
public class RoutineServiceImpl {
    private RoutineRepository routineRepository;

    @Autowired
    public void setRoutineRepository(RoutineRepository repo) {
        this.routineRepository = repo;
    }
}
```

**Por qué no:** Las dependencias son mutables (alguien puede llamar al setter
después de construir el objeto). Solo tiene sentido para dependencias opcionales,
que es un caso raro.

### ✅ Inyección por constructor (siempre esta)

```java
@Service
public class RoutineServiceImpl {
    private final RoutineRepository routineRepository;  // final → inmutable

    // Si solo hay un constructor, @Autowired es opcional (Spring lo detecta solo)
    public RoutineServiceImpl(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }
}
```

**Por qué sí:**
- Dependencias `final` → inmutables, imposible olvidar inyectar una
- Funciona sin Spring: `new RoutineServiceImpl(mockRepository)` → testeable
- El contrato es explícito: el constructor dice exactamente qué necesita la clase

---

## Resumen visual: el ciclo completo

```
Tu código                    Spring al arrancar
──────────────────────────   ──────────────────────────────────────────────
                             1. Escanea clases buscando @Component/@Service/...
@Service                  ←  2. Encuentra RoutineServiceImpl
public class
RoutineServiceImpl {         3. Lee el constructor: necesita
    RoutineRepository,    ←     RoutineRepository, RoutineMapper, RoutineValidator
    RoutineMapper,
    RoutineValidator
}                            4. Busca esos beans en el contenedor
                                (o los crea si aún no existen)

                             5. Llama al constructor con esas instancias

                             6. Guarda el RoutineServiceImpl creado en el contenedor

                             7. Cuando llegue una petición HTTP al RoutineController,
                                Spring inyecta el RoutineServiceImpl ya construido
```

---

## Errores comunes y cómo leerlos

**`NoSuchBeanDefinitionException`**
```
No qualifying bean of type 'RoutineValidator' available
```
Spring no encuentra el bean. Causas: falta `@Component`, el paquete no se escanea,
o el nombre está mal.

**`UnsatisfiedDependencyException`**
```
Error creating bean 'routineServiceImpl': Unsatisfied dependency expressed
through constructor parameter 0
```
Spring no puede crear `routineServiceImpl` porque no puede resolver uno de
sus parámetros del constructor. Busca la causa raíz (nested exception) — suele
ser otro `NoSuchBeanDefinitionException`.

**`BeanCurrentlyInCreationException`**
```
The dependencies of some of the beans form a cycle: routineServiceImpl → X → routineServiceImpl
```
Dependencia circular. A necesita B y B necesita A. Spring no puede resolver
el orden de creación. Solución: refactorizar para romper el ciclo.

---

## Siguiente paso

Con este modelo mental claro, el documento de anotaciones (`spring-annotations.md`)
te resultará mucho más fácil de seguir. Cada anotación es simplemente una forma
de comunicarle algo al contenedor IoC.
