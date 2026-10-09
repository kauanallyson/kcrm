package br.com.kauanallyson.kcrm.config;

import br.com.kauanallyson.kcrm.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A origem permitida vem do default do profile dev (http://localhost:5173)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class CorsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDeOrigemPermitidaRecebeHeadersCorsSemAutenticacao() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")));
    }

    @Test
    void origemNaoListadaNaoRecebeAllowOrigin() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", "https://malicioso.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
