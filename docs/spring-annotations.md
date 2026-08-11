# Anotaciones de Spring Boot — Guia practica

Referencia viva de anotaciones que usamos en el proyecto, explicadas con ejemplos
reales del codigo. Se ira ampliando conforme aparezcan nuevas anotaciones.

---

## Indice

1. [@Configuration](#configuration)
2. [@Bean](#bean)
3. [@Component, @Service, @Repository, @Controller](#estereotipos)
4. [@RestController y @RequestMapping](#rest)

---

## @Configuration

**Que hace:** Marca una clase como "fabrica de configuracion". Le dice a Spring:
"dentro de esta clase hay metodos que producen objetos que quiero que gestiones tu".

**Cuando usarla:** Cuando necesitas crear y configurar objetos que no son simples
servicios/repositorios, sino configuracion tecnica (CORS, seguridad, clientes HTTP,
serializacion, etc.).

**Ejemplo real del proyecto — `CorsConfig.java`:**

```java
@Configuration  // Sin esto, Spring ignora la clase y nunca ejecuta los @Bean de dentro
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        // ...
    }
}
```

**Analogia:** `@Configuration` es el cartel de "ABIERTO" en una tienda. Sin el,
Spring pasa de largo aunque dentro haya productos (`@Bean`).

**Diferencia con `@Component`:** Tecnicamente `@Configuration` incluye `@Component`
(Spring la detecta igual en el escaneo). La diferencia es que `@Configuration`
garantiza que los metodos `@Bean` se ejecutan en modo "proxy": si un `@Bean` llama
a otro `@Bean` dentro de la misma clase, Spring devuelve la misma instancia
(singleton), no crea una nueva. Con `@Component` simple, cada llamada crearia
un objeto nuevo.

---

## @Bean

**Que hace:** Marca un metodo para que Spring ejecute ese metodo al arrancar,
guarde el objeto que devuelve en su contenedor (ApplicationContext), y lo inyecte
automaticamente donde se necesite.

**Cuando usarla:** Cuando necesitas que Spring gestione un objeto que TU construyes
manualmente (porque necesita configuracion especial, o es de una libreria externa
y no puedes ponerle `@Component`).

**Ejemplo real del proyecto:**

```java
@Bean  // "Spring, ejecuta esto al arrancar y guarda el resultado"
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200");
        }
    };
}
```

**Flujo completo:**
1. Spring arranca y escanea las clases `@Configuration`
2. Encuentra el metodo `corsConfigurer()` marcado con `@Bean`
3. Lo ejecuta y guarda el `WebMvcConfigurer` devuelto en su contenedor
4. Cuando Spring configura el servidor web, busca todos los `WebMvcConfigurer`
   en su contenedor y aplica su configuracion (en este caso, las reglas CORS)

**@Bean vs @Component — cuando usar cada una:**

| Situacion | Usa |
|-----------|-----|
| Clase tuya que es un servicio/repositorio/controller | `@Component` (o sus variantes) |
| Objeto que necesita configuracion manual | `@Bean` dentro de `@Configuration` |
| Clase de una libreria externa (no puedes modificarla) | `@Bean` dentro de `@Configuration` |

Ejemplo: no puedes ponerle `@Component` a `WebMvcConfigurer` porque es una interfaz
de Spring, no una clase tuya. Por eso usas `@Bean` para decirle a Spring
"aqui tienes la implementacion que quiero que uses".

---

## @Component, @Service, @Repository, @Controller {#estereotipos}

Estas cuatro son "estereotipos" — variantes de lo mismo con distinto nombre
para expresar intencion.

**Que hacen:** Marcan una clase para que Spring la detecte automaticamente
(component scanning), cree una instancia y la guarde en su contenedor.

```
@Component        <-- generica, "esto es un bean de Spring"
   |
   +-- @Service        <-- "esto es logica de negocio"
   +-- @Repository     <-- "esto accede a datos" (+ traduce excepciones de BD)
   +-- @Controller     <-- "esto maneja peticiones HTTP"
```

**Las cuatro hacen lo mismo a nivel tecnico** (registrar la clase como bean),
pero:
- `@Repository` ademas traduce excepciones de la BD a excepciones de Spring
- `@Controller` ademas habilita el manejo de peticiones web
- `@Service` es puramente semantica (no añade comportamiento extra)

**Ejemplo real del proyecto:**

```java
// Spring detecta esta clase, crea una instancia de RoutineServiceImpl,
// y la inyecta donde alguien pida un RoutineService
@Service
public class RoutineServiceImpl implements RoutineService {
    // ...
}
```

**Por que NO usar `@Component` para todo:** Funciona, pero pierdes claridad.
Al leer `@Service` sabes que es logica de negocio; al leer `@Repository`
sabes que accede a datos. Es documentacion implicita.

---

## @RestController y @RequestMapping {#rest}

**@RestController** = `@Controller` + `@ResponseBody`. Le dice a Spring:
"esta clase maneja peticiones HTTP y todo lo que devuelvan sus metodos
se serializa directamente a JSON (no busques una vista/template HTML)".

**@RequestMapping** = define el prefijo de URL para todos los endpoints
de ese controller.

**Ejemplo real del proyecto:**

```java
@RestController  // Cada metodo devuelve JSON automaticamente
@RequestMapping(ApiConfig.API_BASE_URL + "routines")  // Prefijo: /api/workout-service/routines
public class RoutineController {

    @GetMapping         // GET /api/workout-service/routines
    public List<RoutineResponse> getAll() { ... }

    @GetMapping("/{id}")  // GET /api/workout-service/routines/123
    public RoutineResponse getById(@PathVariable Long id) { ... }

    @PostMapping        // POST /api/workout-service/routines
    public RoutineResponse create(@RequestBody RoutineRequest request) { ... }
}
```

**Sin `@RestController`** tendrias que poner `@ResponseBody` en cada metodo
para que Spring sepa que debe convertir el objeto a JSON en vez de buscar
un archivo HTML con ese nombre.

---

## @Transactional

**Que hace:** Envuelve la ejecucion de un metodo (o todos los metodos publicos de
una clase) dentro de una transaccion de base de datos. Si todo va bien, hace `commit`.
Si se lanza una excepcion, hace `rollback` automatico y la BD queda como estaba.

**Por que importa:** Sin transacciones, si una operacion falla a mitad (ej. guardas
una rutina pero falla al guardar sus ejercicios), la BD queda en un estado
inconsistente — la rutina existe pero sin ejercicios. Con `@Transactional`,
o se guarda TODO o no se guarda NADA.

### A nivel de clase vs a nivel de metodo

**Ejemplo real del proyecto — `RoutineServiceImpl.java`:**

```java
@Service
@Transactional  // TODOS los metodos publicos son transaccionales por defecto
public class RoutineServiceImpl implements RoutineService {

    // Este metodo hereda @Transactional de la clase → abre transaccion,
    // hace el save, y si no hay error hace commit
    @Override
    public RoutineResponse create(Long programId, RoutineRequest request) {
        // Si esto falla, rollback automatico
        RoutineEntity saved = routineRepository.save(routine);
        return routineMapper.toResponse(saved);
    }

    // Este metodo SOBREESCRIBE el @Transactional de la clase con readOnly = true
    @Override
    @Transactional(readOnly = true)
    public RoutineResponse findById(Long id) {
        return routineMapper.toResponse(routineRepository.findById(id)...);
    }
}
```

### readOnly = true — por que usarlo en lecturas

`@Transactional(readOnly = true)` le dice a Hibernate: "este metodo solo lee datos,
no va a modificar nada". Esto permite optimizaciones:

1. **Hibernate desactiva el dirty checking** — normalmente, al final de cada
   transaccion, Hibernate compara todos los objetos cargados con su estado original
   para ver si algo cambio. Con `readOnly`, se salta esa comparacion (menos CPU).
2. **El driver JDBC puede optimizar** — algunos drivers envian la query como
   read-only al servidor de BD, que puede usar replicas de lectura.
3. **Documenta la intencion** — al leer el codigo sabes que ese metodo no modifica datos.

### Errores comunes

- **Llamar un metodo `@Transactional` desde otro metodo de la MISMA clase:**
  No funciona. Spring aplica `@Transactional` mediante un proxy que envuelve la clase.
  Si llamas `this.otroMetodo()`, no pasas por el proxy y la transaccion no se crea.

  ```java
  @Transactional
  public void metodoA() {
      // ESTO NO abre una nueva transaccion para metodoB,
      // porque la llamada es interna (this.metodoB())
      metodoB();
  }

  @Transactional
  public void metodoB() { ... }
  ```

- **Capturar la excepcion dentro del metodo:** Si haces try-catch y te tragas la
  excepcion, Spring no ve que hubo error y hace commit en vez de rollback.

---

## Proximas anotaciones (se añadiran cuando se usen en el proyecto)

- `@Autowired` / inyeccion por constructor — como Spring conecta los beans entre si
- `@Valid` / `@Validated` — validacion de DTOs
- `@ExceptionHandler` / `@ControllerAdvice` — manejo global de errores
- `@EnableKafka`, `@KafkaListener` — cuando se integre Kafka
