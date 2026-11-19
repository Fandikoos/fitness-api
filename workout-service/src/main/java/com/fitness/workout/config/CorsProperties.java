package com.fitness.workout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ConfigurationProperties vincula automáticamente las propiedades del fichero .properties
 * que empiecen por "app.cors" a los campos de este record.
 *
 * Por qué un record y no una clase normal:
 * - Los records de Java son inmutables por diseño (no hay setters), lo que encaja
 *   perfectamente con la configuración: se lee una vez al arrancar y no cambia.
 * - Spring Boot soporta records con @ConfigurationProperties desde la versión 2.6.
 *
 * La propiedad "app.cors.allowed-origins" del .properties activo (dev o prod)
 * se mapea al campo "allowedOrigins" gracias al kebab-case binding de Spring.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(String allowedOrigins) {
}
