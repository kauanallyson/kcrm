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
    void corretorCadastraCliente() throws Exception {
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
    void obrigatoriosEFormatosVoltamJuntos() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"), "{\"whatsapp\":\"123\"}"), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.origem").exists())
                .andExpect(jsonPath("$.errors.whatsapp").exists());
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
        List<String> ids = JsonPath.read(resposta, "$.conteudo[*].id");
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

    @Test
    void listaPaginaComMetadados() throws Exception {
        for (int i = 0; i < 7; i++) {
            cadastrarCliente(corretor, clienteJson(""));
        }
        mockMvc.perform(comToken(get("/api/clientes?page=1&size=5"), corretor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(2))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(5))
                .andExpect(jsonPath("$.totalElementos").value(7))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mockMvc.perform(comToken(get("/api/clientes?page=0&size=5"), corretor))
                .andExpect(jsonPath("$.conteudo.length()").value(5));
    }

    @Test
    void tamanhoPadrao20EMaximo100() throws Exception {
        mockMvc.perform(comToken(get("/api/clientes"), corretor))
                .andExpect(jsonPath("$.tamanho").value(20));
        mockMvc.perform(comToken(get("/api/clientes?size=1000"), corretor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanho").value(100));
    }

    @Test
    void nenhumaPaginaMostraItensDeOutroCorretor() throws Exception {
        String outro = api.novoCorretor();
        for (int i = 0; i < 3; i++) {
            cadastrarCliente(outro, clienteJson(""));
        }
        UUID meu = cadastrarCliente(corretor, clienteJson(""));

        for (int pagina = 0; pagina < 4; pagina++) {
            String resposta = mockMvc.perform(comToken(get("/api/clientes?size=1&page=" + pagina), corretor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andReturn().getResponse().getContentAsString();
            List<String> ids = JsonPath.read(resposta, "$.conteudo[*].id");
            assertThat(ids).isSubsetOf(meu.toString());
        }
    }

    @Test
    void ordenacaoPorCampoInexistenteEhRequisicaoInvalida() throws Exception {
        mockMvc.perform(comToken(get("/api/clientes?sort=naoExiste"), corretor))
                .andExpect(status().isBadRequest());
    }
}
