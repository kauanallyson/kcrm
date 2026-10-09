package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.auth.*;
import br.com.kauanallyson.kcrm.ratelimit.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            AuthenticatedUserService authenticatedUserService,
            SecurityProblemHandler problemHandler,
            RateLimitFilter rateLimitFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // Usa o bean corsConfigurationSource; o preflight é respondido antes da autenticação
                .cors(cors -> {})
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AuthPaths.PUBLIC.toArray(String[]::new)).permitAll()
                        .requestMatchers("/error").permitAll()
                        // Liberado só porque a porta de management não é exposta pelo Traefik
                        .requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, authenticatedUserService),
                        UsernamePasswordAuthenticationFilter.class)
                // Barra o excesso antes de gastar uma consulta no banco resolvendo o token
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
                .build();
    }

    // Origens exatas vindas de CORS_ALLOWED_ORIGINS; vazia não libera nenhuma. Sem cookies cross-site
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${kcrm.cors.allowed-origins:}") List<String> allowedOrigins
    ) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins.stream().map(String::trim).filter(o -> !o.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Sem isso o navegador esconde do frontend o Location do 201, o Retry-After do 429 e o id da requisição
        config.setExposedHeaders(List.of("Location", "Retry-After", "X-Request-Id"));
        config.setMaxAge(Duration.ofHours(1));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
