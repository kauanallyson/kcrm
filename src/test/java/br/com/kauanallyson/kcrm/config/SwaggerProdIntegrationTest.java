package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prod exige as credenciais do banco no ambiente; a conexão de verdade vem do Testcontainers
@SpringBootTest(properties = {"DB_USERNAME=test", "DB_PASSWORD=test"})
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Import(TestcontainersConfig.class)
class SwaggerProdIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsNaoExisteEmProd() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    void swaggerUiNaoExisteEmProd() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }
}
