package br.com.kauanallyson.kcrm;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    }

    @Bean
    DynamicPropertyRegistrar jwtSecret() {
        String secret = TestJwt.randomSecret();
        return registry -> registry.add("jwt.secret", () -> secret);
    }

    @Bean
    DynamicPropertyRegistrar administrador() {
        return registry -> {
            registry.add("kcrm.administrador.email", () -> TestApi.ADMINISTRADOR_EMAIL);
            registry.add("kcrm.administrador.senha", () -> TestApi.ADMINISTRADOR_SENHA);
        };
    }

    @Bean
    DynamicPropertyRegistrar smtp() {
        return registry -> registry.add("spring.mail.port", TestEmail::porta);
    }

    // Todos os testes saem do mesmo IP; o limite de produção derrubaria a suíte
    @Bean
    DynamicPropertyRegistrar rateLimitFolgado() {
        return registry -> {
            registry.add("kcrm.rate-limit.auth.requests", () -> 10_000);
            registry.add("kcrm.rate-limit.api.requests", () -> 10_000);
        };
    }

}
