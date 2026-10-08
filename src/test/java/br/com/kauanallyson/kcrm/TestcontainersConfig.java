package br.com.kauanallyson.kcrm;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

    @Bean
    DynamicPropertyRegistrar jwtSecret() {
        String secret = TestJwt.randomSecret();
        return registry -> registry.add("jwt.secret", () -> secret);
    }

    // Admin inicial criado na subida, para os testes de integração logarem como Admin
    @Bean
    DynamicPropertyRegistrar adminInicialProperties() {
        return registry -> {
            registry.add("admin.nome", () -> "Admin Inicial");
            registry.add("admin.email", () -> TestApi.ADMIN_EMAIL);
            registry.add("admin.senha", () -> TestApi.ADMIN_SENHA);
            registry.add("admin.cpf", () -> "529.982.247-25");
            registry.add("admin.telefone", () -> "(88) 99999-0000");
            registry.add("admin.endereco.rua", () -> "Rua A");
            registry.add("admin.endereco.numero", () -> "1");
            registry.add("admin.endereco.bairro", () -> "Centro");
            registry.add("admin.endereco.cidade", () -> "Sobral");
            registry.add("admin.endereco.estado", () -> "CE");
            registry.add("admin.endereco.cep", () -> "62010-000");
        };
    }
}
