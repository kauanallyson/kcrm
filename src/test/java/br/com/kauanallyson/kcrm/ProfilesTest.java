package br.com.kauanallyson.kcrm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilesTest {

    @Test
    void prodNaoSobeSemCredenciaisDoBanco() {
        // Sem variáveis de ambiente, como numa máquina de produção mal configurada
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        SpringApplication app = new SpringApplication(KcrmApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setEnvironment(env);

        assertThatThrownBy(() -> app.run("--spring.profiles.active=prod"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_USERNAME")
                .hasMessageContaining("DB_PASSWORD");
    }
}
