package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.auth.AuthPaths;
import br.com.kauanallyson.kcrm.auth.AuthenticatedUserService;
import br.com.kauanallyson.kcrm.auth.JwtAuthenticationFilter;
import br.com.kauanallyson.kcrm.auth.JwtService;
import br.com.kauanallyson.kcrm.auth.SecurityProblemHandler;
import br.com.kauanallyson.kcrm.ratelimit.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AuthPaths.PUBLIC.toArray(String[]::new)).permitAll()
                        .requestMatchers("/error").permitAll()
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
