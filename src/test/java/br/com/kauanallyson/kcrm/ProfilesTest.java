package br.com.kauanallyson.kcrm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
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
                .hasMessageContaining("DB_PASSWORD")
                .hasMessageContaining("SMTP_PASSWORD")
                .hasMessageContaining("CONFIRMACAO_URL_BASE");
    }

    @Test
    void soODevTemDefaultsParaOsSegredos() throws IOException {
        assertThat(propriedades("application-dev")).containsKeys("DB_USERNAME", "DB_PASSWORD");
        assertThat(propriedades("application-prod")).doesNotContainKeys("DB_USERNAME", "DB_PASSWORD");
        assertThat(propriedades("application")).doesNotContainKeys("DB_USERNAME", "DB_PASSWORD");
        assertThat(propriedades("application-dev")).containsKeys("SMTP_HOST", "SMTP_PASSWORD", "CONFIRMACAO_URL_BASE");
        assertThat(propriedades("application-prod")).doesNotContainKeys("SMTP_HOST", "SMTP_PASSWORD", "CONFIRMACAO_URL_BASE");
    }

    @Test
    void showSqlSoNoDev() throws IOException {
        assertThat(propriedades("application").getProperty("spring.jpa.show-sql")).isEqualTo("false");
        assertThat(propriedades("application-dev").getProperty("spring.jpa.show-sql")).isEqualTo("true");
        assertThat(propriedades("application-prod").getProperty("spring.jpa.show-sql")).isEqualTo("false");
    }

    @Test
    void semProfileAtivoRodaComoDev() throws IOException {
        assertThat(propriedades("application").getProperty("spring.profiles.default")).isEqualTo("dev");
    }

    private static java.util.Properties propriedades(String arquivo) throws IOException {
        return PropertiesLoaderUtils.loadProperties(new ClassPathResource(arquivo + ".properties"));
    }
}
