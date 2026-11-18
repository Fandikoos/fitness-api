package com.fitness.workout.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI (Swagger).
 *
 * springdoc-openapi escanea automáticamente los @RestController y genera la spec,
 * pero este bean permite personalizar los metadatos generales del API (título,
 * descripción, versión) que aparecen en la cabecera de Swagger UI.
 *
 * Acceso una vez arrancado el servicio:
 *   - Swagger UI:  http://localhost:8080/swagger-ui.html
 *   - JSON spec:   http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workoutServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workout Service API")
                        .description("Microservicio de gestión de programas, rutinas, ejercicios y sesiones de entrenamiento")
                        .version("1.0.0"));
    }
}
