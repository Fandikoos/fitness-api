# Fitness API

![CI](https://github.com/Fandikoos/fitness-api/actions/workflows/ci.yml/badge.svg)

Backend de **Fitness App**: microservicios con Spring Boot 4 (Java 21). Proyecto de aprendizaje de arquitectura de microservicios.

- `workout-service`: programas, rutinas, catálogo de ejercicios, series y sesiones de entrenamiento.
- Puesta en marcha en local: [docs/setup-local.md](docs/setup-local.md).
- Convenciones del código: [CLAUDE.md](CLAUDE.md).

## Flujo de trabajo

Este repo sigue **GitHub Flow**: `main` siempre está estable y todo cambio entra por Pull Request.

### Ramas
- `main`: protegida. No admite push directo, force push ni borrado.
- Una rama por tarjeta de Trello, creada desde `main` actualizado:

| Prefijo     | Uso                                   | Ejemplo                          |
|-------------|---------------------------------------|----------------------------------|
| `feat/`     | Funcionalidad nueva                   | `feat/historial-sesiones`        |
| `fix/`      | Corrección de un bug                  | `fix/validaciones-series`        |
| `refactor/` | Cambio interno sin cambiar comportamiento | `refactor/routine-service`   |
| `docs/`     | Solo documentación                    | `docs/flujo-de-trabajo`          |
| `chore/`    | Configuración, dependencias, CI       | `chore/ci-backend`               |

### Ciclo de una tarjeta
```bash
git switch main && git pull                # partir de main actualizado
git switch -c fix/validaciones-series      # rama de la tarjeta
# ...commits...
git push -u origin fix/validaciones-series
gh pr create --fill                        # o desde la web de GitHub
```

1. Abre el PR hacia `main` y rellena la plantilla (qué cambia, tarjeta, cómo probarlo).
2. Espera a que pasen los checks de la CI (cuando exista).
3. Fusiona con **Squash and merge**: `main` queda con un commit por tarjeta.
4. GitHub borra la rama remota automáticamente. En local: `git switch main && git pull && git branch -d <rama>`.

### Commits y títulos de PR
En español y con el formato `tipo: descripción` (`feat`, `fix`, `refactor`, `docs`, `chore`, `test`).
El título del PR es el que acaba como commit en `main` al hacer squash, así que cuídalo más que los commits intermedios.
