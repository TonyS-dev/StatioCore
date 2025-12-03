package org.codeup.parknexus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for frontend integration.
 * Allows React (3000), Vite (5173), Angular (4200) dev servers.
 *
 * Production: Replace with actual frontend domain.
 *
 * @author TonyS-dev
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Frontend development servers and Docker container
        configuration.setAllowedOrigins(List.of(
                "http://localhost",        // Docker frontend (port 80)
                "http://localhost:3000",   // React dev server
                "http://localhost:5173",   // Vite dev server
                "http://localhost:4200"    // Angular dev server
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
