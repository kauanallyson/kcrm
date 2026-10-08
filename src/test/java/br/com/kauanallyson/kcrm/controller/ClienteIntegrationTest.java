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
    private String admin;
    private UUID corretorId;
    private String corretor;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestApi(mockMvc);
        admin = api.loginAdminInicial();
        String email = randomEmail();
        corretorId = api.cadastrar(email, "CORRETOR");
        corretor = api.login(email, SENHA);
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

    private String outroCorretor() throws Exception {
        String email = randomEmail();
        api.cadastrar(email, "CORRETOR");
        return api.login(email, SENHA);
    }

    @Test
    void corretorCadastraClienteEPassaAAtendeLo() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"),
                        clienteJson(",\"origem\":\"INDICACAO\",\"indicadoPor\":\"joão DA silva\"")), corretor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.corretorId").value(corretorId.toString()))
                .andExpect(jsonPath("$.whatsapp").value("(88) 99999-1111"))
                .andExpect(jsonPath("$.indicadoPor").value("João da Silva"))
                .andExpect(jsonPath("$.cpf").isEmpty())
                .andExpect(jsonPath("$.endereco").isEmpty());
    }

    @Test
    void corretorNaoEscolheOCorretor() throws Exception {
        UUID outro = api.cadastrar(randomEmail(), "CORRETOR");

        mockMvc.perform(comToken(json(post("/api/clientes"), clienteJson(",\"corretorId\":\"" + outro + "\"")), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.corretorId").exists());
    }

    @Test
    void adminCadastraClienteComCorretorId() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"), clienteJson(",\"corretorId\":\"" + corretorId + "\"")), admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.corretorId").value(corretorId.toString()));
    }

    @Test
    void adminSemCorretorIdRecebe400() throws Exception {
        mockMvc.perform(comToken(json(post("/api/clientes"), clienteJson("")), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.errors.corretorId").exists());
    }

    @Test
    void adminNaoCadastraClienteParaOutroAdmin() throws Exception {
        UUID outroAdmin = api.cadastrar(randomEmail(), "ADMIN");

        mockMvc.perform(comToken(json(post("/api/clientes"), clienteJson(",\"corretorId\":\"" + outroAdmin + "\"")), admin))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("CORRETOR_INVALIDO"));
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
        UUID id = cadastrarCliente(outroCorretor(), clienteJson(""));

        mockMvc.perform(comToken(get("/api/clientes/" + id), corretor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CLIENTE_NAO_ENCONTRADO"));
        mockMvc.perform(comToken(json(put("/api/clientes/" + id), clienteJson("")), corretor))
                .andExpect(status().isNotFound());
        mockMvc.perform(comToken(get("/api/clientes/" + id + "/historico"), corretor))
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
    void listaMostraAoCorretorSoOsSeusEAoAdminTodos() throws Exception {
        UUID meu = cadastrarCliente(corretor, clienteJson(""));
        UUID alheio = cadastrarCliente(outroCorretor(), clienteJson(""));

        String doCorretor = mockMvc.perform(comToken(get("/api/clientes"), corretor))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> idsDoCorretor = JsonPath.read(doCorretor, "$[*].id");
        assertThat(idsDoCorretor).containsExactly(meu.toString());

        String doAdmin = mockMvc.perform(comToken(get("/api/clientes"), admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> idsDoAdmin = JsonPath.read(doAdmin, "$[*].id");
        assertThat(idsDoAdmin).contains(meu.toString(), alheio.toString());
    }

    @Test
    void adminTransfereClienteEOEventoEhRegistrado() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));
        String novoEmail = randomEmail();
        UUID novoId = api.cadastrar(novoEmail, "CORRETOR");

        mockMvc.perform(comToken(json(post("/api/clientes/" + id + "/transferencia"),
                        "{\"corretorId\":\"" + novoId + "\",\"motivo\":\"Férias\"}"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corretorId").value(novoId.toString()));

        mockMvc.perform(comToken(get("/api/clientes/" + id + "/historico"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$[0].motivo").value("Férias"))
                .andExpect(jsonPath("$[0].detalhe").value("Test -> Test"))
                .andExpect(jsonPath("$[0].autorNome").value("Admin Inicial"));

        // O Corretor anterior deixa de ver o Cliente; o novo passa a ver, com o Histórico
        mockMvc.perform(comToken(get("/api/clientes/" + id), corretor)).andExpect(status().isNotFound());
        mockMvc.perform(comToken(get("/api/clientes/" + id + "/historico"), api.login(novoEmail, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("TRANSFERENCIA"));
    }

    @Test
    void corretorRecebe403NaTransferencia() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));
        UUID outro = api.cadastrar(randomEmail(), "CORRETOR");

        mockMvc.perform(comToken(json(post("/api/clientes/" + id + "/transferencia"),
                        "{\"corretorId\":\"" + outro + "\"}"), corretor))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferenciaParaUsuarioDesativadoOuAdminEhRejeitada() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));
        UUID desativado = api.cadastrar(randomEmail(), "CORRETOR");
        mockMvc.perform(comToken(json(post("/api/usuarios/" + desativado + "/desativacao"), "{\"motivo\":\"saiu\"}"), admin))
                .andExpect(status().isOk());
        UUID outroAdmin = api.cadastrar(randomEmail(), "ADMIN");

        for (UUID alvo : List.of(desativado, outroAdmin)) {
            mockMvc.perform(comToken(json(post("/api/clientes/" + id + "/transferencia"),
                            "{\"corretorId\":\"" + alvo + "\"}"), admin))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.code").value("CORRETOR_INVALIDO"));
        }
    }

    @Test
    void transferenciaParaOMesmoCorretorEhConflito() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));

        mockMvc.perform(comToken(json(post("/api/clientes/" + id + "/transferencia"),
                        "{\"corretorId\":\"" + corretorId + "\"}"), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSFERENCIA_PARA_O_MESMO_CORRETOR"));
    }

    @Test
    void clienteNaoPodeSerApagado() throws Exception {
        UUID id = cadastrarCliente(corretor, clienteJson(""));

        mockMvc.perform(comToken(delete("/api/clientes/" + id), admin))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(404, 405));
    }
}
