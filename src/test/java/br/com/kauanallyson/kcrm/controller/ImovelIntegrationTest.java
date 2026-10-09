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
class ImovelIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private TestApi api;
    private String corretor;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestApi(mockMvc);
        corretor = api.novoCorretor();
    }

    private static String imovelJson(String preco, String extra) {
        return """
                {"tipo":"CASA","endereco":%s,"precoVenda":%s,
                 "proprietario":{"nome":"Ana","whatsapp":"88999991111","email":"ana@test.com","cpf":"%s"}%s}
                """.formatted(ENDERECO_PADRAO, preco, randomCpf(), extra);
    }

    private UUID cadastrarImovel(String token) throws Exception {
        String resposta = mockMvc.perform(comToken(json(post("/api/imoveis"), imovelJson("250000", "")), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resposta, "$.id"));
    }

    @Test
    void corretorCadastraImovelDisponivel() throws Exception {
        mockMvc.perform(comToken(json(post("/api/imoveis"),
                        imovelJson("250000.50", ",\"area\":200,\"frente\":10,\"fundo\":20,\"quartos\":3,\"suites\":1,\"banheiros\":2,\"vagas\":2")), corretor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.precoVenda").value(250000.50))
                .andExpect(jsonPath("$.quartos").value(3))
                .andExpect(jsonPath("$.proprietario.whatsapp").value("(88) 99999-1111"))
                .andExpect(jsonPath("$.endereco.cidade").value("Sobral"));
    }

    @Test
    void caracteristicasSaoOpcionais() throws Exception {
        mockMvc.perform(comToken(json(post("/api/imoveis"), imovelJson("1000", "")), corretor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.area").isEmpty())
                .andExpect(jsonPath("$.vagas").isEmpty());
    }

    @Test
    void camposInvalidosRetornamErrosPorCampo() throws Exception {
        String body = """
                {"tipo":"CASA","endereco":%s,"precoVenda":100,
                 "proprietario":{"nome":"Ana","whatsapp":"123","email":"x","cpf":"111"}}
                """.formatted(ENDERECO_PADRAO);
        mockMvc.perform(comToken(json(post("/api/imoveis"), body), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['errors']['proprietario.whatsapp']").exists())
                .andExpect(jsonPath("$['errors']['proprietario.email']").exists())
                .andExpect(jsonPath("$['errors']['proprietario.cpf']").exists());

        mockMvc.perform(comToken(json(post("/api/imoveis"), imovelJson("0", "")), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.precoVenda").exists());
    }

    @Test
    void obrigatoriosAusentesVoltamJuntos() throws Exception {
        mockMvc.perform(comToken(json(post("/api/imoveis"), "{}"), corretor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tipo").exists())
                .andExpect(jsonPath("$.errors.endereco").exists())
                .andExpect(jsonPath("$.errors.precoVenda").exists())
                .andExpect(jsonPath("$.errors.proprietario").exists());
    }

    @Test
    void tipoInexistenteEhRequisicaoInvalida() throws Exception {
        mockMvc.perform(comToken(json(post("/api/imoveis"), imovelJson("1000", "").replace("CASA", "CASTELO")), corretor))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corretorEditaOProprioImovel() throws Exception {
        UUID id = cadastrarImovel(corretor);

        mockMvc.perform(comToken(json(put("/api/imoveis/" + id), imovelJson("300000", ",\"quartos\":4")), corretor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoVenda").value(300000))
                .andExpect(jsonPath("$.quartos").value(4));
    }

    @Test
    void vendidoNaoEditaNemVoltaEPodeSerApagado() throws Exception {
        UUID id = cadastrarImovel(corretor);

        mockMvc.perform(comToken(post("/api/imoveis/" + id + "/venda"), corretor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("VENDIDO"));
        mockMvc.perform(comToken(json(put("/api/imoveis/" + id), imovelJson("1", "")), corretor))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IMOVEL_VENDIDO"));
        mockMvc.perform(comToken(post("/api/imoveis/" + id + "/venda"), corretor))
                .andExpect(status().isConflict());

        mockMvc.perform(comToken(delete("/api/imoveis/" + id), corretor))
                .andExpect(status().isNoContent());
        mockMvc.perform(comToken(get("/api/imoveis/" + id), corretor))
                .andExpect(status().isNotFound());
    }

    @Test
    void corretorNaoVeEditaNemApagaImovelDeOutro() throws Exception {
        UUID id = cadastrarImovel(api.novoCorretor());

        mockMvc.perform(comToken(get("/api/imoveis/" + id), corretor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("IMOVEL_NAO_ENCONTRADO"));
        mockMvc.perform(comToken(json(put("/api/imoveis/" + id), imovelJson("1", "")), corretor))
                .andExpect(status().isNotFound());
        mockMvc.perform(comToken(post("/api/imoveis/" + id + "/venda"), corretor))
                .andExpect(status().isNotFound());
        mockMvc.perform(comToken(delete("/api/imoveis/" + id), corretor))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaMostraSoOsDoCorretor() throws Exception {
        UUID meu = cadastrarImovel(corretor);
        cadastrarImovel(api.novoCorretor());

        String resposta = mockMvc.perform(comToken(get("/api/imoveis"), corretor))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$[*].id");
        assertThat(ids).containsExactly(meu.toString());
    }
}
