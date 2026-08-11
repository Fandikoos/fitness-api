# Cómo arrancar el proyecto en local

## Requisitos previos

- **Java 21** — el lenguaje en el que está escrito el backend.
- **Docker Desktop** — para levantar la base de datos MySQL sin instalarla directamente en tu máquina.

---

## ¿Qué es Docker y por qué lo usamos?

Imagina que para desarrollar necesitas MySQL instalado en tu PC. Tendrías que:
1. Descargar el instalador de MySQL
2. Configurar el puerto, usuario, contraseña
3. Crear la base de datos manualmente
4. Rezar para que no entre en conflicto con otra versión que ya tengas instalada

**Docker resuelve todo eso.** En lugar de instalar MySQL en tu sistema operativo,
Docker crea un "contenedor" — un entorno aislado y desechable que contiene MySQL
ya configurado y listo para usar.

### Conceptos clave (solo los que necesitas)

| Concepto     | Qué es                                                                                     |
| ------------ | ------------------------------------------------------------------------------------------ |
| **Imagen**   | Una "plantilla" con todo lo necesario para ejecutar un programa (ej. `mysql:latest`)       |
| **Contenedor** | Una instancia en ejecución de una imagen. Es como una mini-máquina virtual pero mucho más ligera |
| **compose.yaml** | Un archivo que describe qué contenedores necesita tu proyecto y cómo configurarlos     |
| **Puerto**   | Cómo tu aplicación "habla" con el contenedor. MySQL usa el puerto 3307 en tu PC (mapeado al 3306 interno del contenedor) |

### ¿Qué pasa cuando haces `docker compose up`?

```
Tu PC                           Contenedor Docker
┌──────────────┐               ┌──────────────────┐
│              │               │                  │
│  Spring Boot │──port 3307──▶│  MySQL Server    │
│  (tu app)    │               │  (workout_db)    │
│              │               │                  │
└──────────────┘               └──────────────────┘
```

Docker descarga la imagen de MySQL (solo la primera vez), crea un contenedor con
MySQL corriendo, crea la base de datos `workout_db` automáticamente, y expone el
puerto 3307 en tu máquina (mapeado al 3306 interno del contenedor) para que tu app
Spring Boot se conecte.

---

## Paso a paso

### 1. Instalar Docker Desktop

Descarga Docker Desktop desde la página oficial de Docker e instálalo.
Una vez instalado, ábrelo y espera a que el motor de Docker esté corriendo
(verás el icono de la ballena en la barra de tareas sin errores).

Para verificar que funciona, abre una terminal y ejecuta:

```bash
docker --version
```

Deberías ver algo como `Docker version 27.x.x`.

### 2. Configurar variables de entorno

El proyecto usa un archivo `.env` para las credenciales de la base de datos
(nunca se guardan directamente en el código por seguridad).

En la carpeta `workout-service/` ya existe un archivo `.env` con estos valores:

```env
DB_USERNAME=root
DB_PASSWORD=admin
MYSQL_ROOT_PASSWORD=admin
```

- `DB_USERNAME` / `DB_PASSWORD` — los usa Spring Boot para conectarse a MySQL.
- `MYSQL_ROOT_PASSWORD` — lo usa Docker para configurar la contraseña de MySQL al crear el contenedor.

### 3. Levantar MySQL con Docker

Desde la carpeta `workout-service/`:

```bash
docker compose up -d
```

- `up` — crea y arranca los contenedores definidos en `compose.yaml`.
- `-d` — modo "detached" (en segundo plano). Sin esto, la terminal queda ocupada mostrando logs.

**Primera vez:** Docker descarga la imagen de MySQL (~500MB). Las siguientes veces
arranca en segundos porque la imagen ya está descargada.

Para verificar que MySQL está corriendo:

```bash
docker compose ps
```

Deberías ver el servicio `mysql` con estado `running`.

### 4. Arrancar la aplicación Spring Boot

Desde la misma carpeta `workout-service/`:

```bash
./mvnw spring-boot:run
```

Spring Boot se conecta a MySQL automáticamente y Hibernate crea todas las tablas
basándose en las entidades JPA (`@Entity`). Verás las queries SQL en la consola
porque tenemos `spring.jpa.show-sql=true`.

La aplicación estará disponible en `http://localhost:8080`.

### 5. Probar que funciona

Puedes hacer una petición para crear un ejercicio:

```bash
curl -X POST http://localhost:8080/api/exercises \
  -H "Content-Type: application/json" \
  -d '{"name": "Press Banca", "muscleGroup": "CHEST", "exerciseType": "STRENGTH"}'
```

---

## Comandos útiles de Docker Compose

| Comando                    | Qué hace                                                        |
| -------------------------- | --------------------------------------------------------------- |
| `docker compose up -d`     | Arranca los contenedores en segundo plano                       |
| `docker compose down`      | Para y elimina los contenedores (los datos se pierden)          |
| `docker compose stop`      | Para los contenedores sin eliminarlos (los datos se mantienen)  |
| `docker compose start`     | Reanuda contenedores previamente parados con `stop`             |
| `docker compose ps`        | Muestra el estado de los contenedores                           |
| `docker compose logs -f`   | Muestra los logs en tiempo real (Ctrl+C para salir)             |

### ¿Qué pasa con mis datos?

- `docker compose stop` → los datos **se mantienen**. Es como apagar MySQL.
- `docker compose down` → los datos **se pierden**. El contenedor se elimina por completo.
- `docker compose down -v` → elimina también los volúmenes (datos persistidos).

Si quieres que tus datos sobrevivan a un `down`, necesitas configurar un volumen
en el `compose.yaml`. Por ahora no es necesario — Hibernate recrea las tablas cada
vez que arranca gracias a `ddl-auto=update`.

---

## Solución de problemas comunes

### "Port 3307 already in use"
Algo ya está usando el puerto 3307 en tu máquina. Opciones:
- Identifica y para el proceso que lo usa
- O cambia el puerto en `compose.yaml` (ej. `3308:3306`) y actualiza la URL en `application.properties` para que coincida

### "Connection refused" al arrancar Spring Boot
MySQL puede tardar unos segundos en estar listo. Espera un momento y vuelve a arrancar.
Verifica con `docker compose ps` que el contenedor está `running`.

### Docker Desktop no arranca
En Windows, Docker necesita WSL 2 (Windows Subsystem for Linux). Si no lo tienes,
Docker Desktop te pedirá instalarlo durante la primera ejecución.
