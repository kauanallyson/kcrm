package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sem profile ativo roda como dev: a documentação continua pública
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class SwaggerDevIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsDisponivelEmDev() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void swaggerUiDisponivelEmDev() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
