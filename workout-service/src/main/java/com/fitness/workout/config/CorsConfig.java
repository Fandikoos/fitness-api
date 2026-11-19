package com.fitness.workout.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @EnableConfigurationProperties registra CorsProperties en el contexto de Spring.
 * Sin esto, el record anotado con @ConfigurationProperties no se instanciaría y
 * no podríamos inyectarlo aquí. Es la forma de "activar" una clase de propiedades
 * sin necesidad de añadir @ConfigurationPropertiesScan a toda la aplicación.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    // Spring inyecta CorsProperties con el valor de "app.cors.allowed-origins"
    // del perfil activo (dev = localhost:4200 / prod = variable de entorno CORS_ORIGIN).
    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    // "/**" = aplica a todas las rutas del API
                    .addMapping("/**")
                    // El origen ya no está hardcodeado: viene del perfil activo
                    .allowedOrigins(corsProperties.allowedOrigins())
                    // Métodos HTTP que el frontend puede usar
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    // Permite cualquier header en las peticiones (ej. Content-Type, Authorization)
                    .allowedHeaders("*")
                    // Permite enviar cookies/credenciales entre dominios (útil si añades auth más adelante)
                    .allowCredentials(true);
            }
        };
    }
}
