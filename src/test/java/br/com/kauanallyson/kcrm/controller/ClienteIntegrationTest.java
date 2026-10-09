package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static br.com.kauanallyson.kcrm.TestApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class ClienteIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private TestApi api;
    private String corretor;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestApi(mockMvc);
        corretor = api.novoCorretor();
    }

    private static String clienteJson(String extra) {
        return """
                {"nome":"Maria","whatsapp":"88999991111","origem":"SITE"%s}
                """.formatted(extra);
    }

    private UUID cadastrarCliente(String token, String body) throws Exception {
        String resposta = mockMvc.perform(comToken(json(post("/api/clientes"), body), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resposta, "$.id"));
    }

    @Test
    void corretorCadastraClienteEPassaAAtendeLo() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"),
                        clienteJson(",\"origem\":\"INDICACAO\",\"indicadoPor\":\"joão DA silva\"")), corretor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.whatsapp").value("(88) 99999-1111"))
                .andExpect(jsonPath("$.indicadoPor").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").isEmpty())
                .andExpect(jsonPath("$.endereco").isEmpty());
    }

    @Test
    void camposInvalidosRetornamErrosPorCampo() throws Exception {
        String body = """
                {"nome":"Maria","whatsapp":"123","origem":"SITE","cpf":"111","email":"x"}
                """;
        mockMvc.perform(comToken(json(post("/api/clientes"), body), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.whatsapp").exists())
                .andExpect(jsonPath("$.errors.cpf").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void indicadoPorForaDaIndicacaoRecebe400() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"), clienteJson(",\"indicadoPor\":\"João\"")), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.indicadoPor").exists());
    }

    @Test
    void whatsappDuplicadoEhPermitido() throws Exception {
        UUID primeiro = cadastrarCliente(corretor, clienteJson(""));
        UUID segundo = cadastrarCliente(corretor, clienteJson(""));

        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void corretorNaoVeNemEditaClienteDeOutroCorretor() throws Exception {
        UUID id = cadastrarCliente(api.novoCorretor(), clienteJson(""));

        mockMvc.perform(comToken(get("/api/clientes/" + id), corretor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CLIENTE_NAO_ENCONTRADO"));
        mockMvc.perform(comToken(json(put("/api/clientes/" + id), clienteJson("")), corretor))
                .andExpect(status().isNotFound());
    }

    @Test
    void corretorEditaOProprioCliente() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));
        String body = """
                {"nome":"Maria Souza","whatsapp":"88999992222","origem":"SITE","cpf":"%s","email":"maria@test.com","endereco":%s}
                """.formatted(randomCpf(), ENDERECO_PADRAO);

        mockMvc.perform(comToken(json(put("/api/clientes/" + id), body), corretor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("maria@test.com"))
                .andExpect(jsonPath("$.endereco.cidade").value("Sobral"));
    }

    @Test
    void listaMostraAoCorretorSoOsSeus() throws Exception {
        UUID meu = cadastrarCliente(corretor, clienteJson(""));
        cadastrarCliente(api.novoCorretor(), clienteJson(""));

        String resposta = mockMvc.perform(comToken(get("/api/clientes"), corretor))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$[*].id");
        assertThat(ids).containsExactly(meu.toString());
    }

    @Test
    void corretorApagaOProprioClienteMasNaoODeOutro() throws Exception {
        UUID meu = cadastrarCliente(corretor, clienteJson(""));
        UUID alheio = cadastrarCliente(api.novoCorretor(), clienteJson(""));

        mockMvc.perform(comToken(delete("/api/clientes/" + alheio), corretor))
                .andExpect(status().isNotFound());
        mockMvc.perform(comToken(delete("/api/clientes/" + meu), corretor))
                .andExpect(status().isNoContent());
        mockMvc.perform(comToken(get("/api/clientes/" + meu), corretor))
                .andExpect(status().isNotFound());
    }
}
