package com.fitness.workout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Indica a Spring que esta clase contiene beans de configuración
public class CorsConfig {

    @Bean // Registra este WebMvcConfigurer en el contexto de Spring para que aplique la config
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    // "/**" = aplica a todas las rutas del API
                    .addMapping("/**")
                    // Orígenes permitidos: solo nuestro frontend Angular en desarrollo
                    .allowedOrigins("http://localhost:4200")
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
