package br.com.kauanallyson.kcrm;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "DB_POOL_MAX=7")
@Import(TestcontainersConfig.class)
class KcrmApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
    }

    @Test
    void poolDoHikariVemDasVariaveisDeAmbienteComDefaults() {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        assertThat(hikari.getMaximumPoolSize()).isEqualTo(7);
        assertThat(hikari.getMinimumIdle()).isEqualTo(2);
        assertThat(hikari.getConnectionTimeout()).isEqualTo(5000);
        assertThat(hikari.getIdleTimeout()).isEqualTo(300000);
        assertThat(hikari.getMaxLifetime()).isEqualTo(1800000);
    }

    @Test
    void desligaDeFormaGraciosa() {
        assertThat(environment.getProperty("server.shutdown")).isEqualTo("graceful");
        assertThat(environment.getProperty("spring.lifecycle.timeout-per-shutdown-phase")).isEqualTo("20s");
    }

}
