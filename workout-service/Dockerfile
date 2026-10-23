# === Etapa 1: Build ===
# Usa una imagen con Maven + JDK 21 para compilar el proyecto.
# El "multi-stage build" permite que la imagen final sea mucho más ligera,
# porque solo incluye el JAR compilado, no Maven ni el código fuente.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero copiamos solo el pom.xml y descargamos dependencias.
# Docker cachea cada capa (COPY, RUN...) y solo la reconstruye si cambia.
# Como el pom.xml cambia poco, esta capa se reutiliza casi siempre,
# evitando descargar dependencias en cada build.
COPY pom.xml .
RUN mvn dependency:go-offline

# Ahora copiamos el código fuente y compilamos.
# Esta capa sí se reconstruye cada vez que cambias código.
COPY src ./src
RUN mvn package -DskipTests

# === Etapa 2: Runtime ===
# Imagen ligera solo con el JRE (no necesitamos el JDK completo ni Maven).
# eclipse-temurin es la distribución de OpenJDK recomendada para producción.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
