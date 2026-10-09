package br.com.kauanallyson.kcrm.auditoria;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestEmail;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static br.com.kauanallyson.kcrm.TestApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class AuditoriaIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    @Test
    void respostaDevolveORequestIdRecebido() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("X-Request-Id", "abc-123"))
                .andExpect(header().string("X-Request-Id", "abc-123"));
    }

    @Test
    void semRequestIdOuComValorInseguroGeraUmUuid() throws Exception {
        String gerado = mockMvc.perform(get("/api/auth/me"))
                .andReturn().getResponse().getHeader("X-Request-Id");
        assertThat(UUID.fromString(gerado)).isNotNull();

        String trocado = mockMvc.perform(get("/api/auth/me").header("X-Request-Id", "a\nlinha falsa"))
                .andReturn().getResponse().getHeader("X-Request-Id");
        assertThat(UUID.fromString(trocado)).isNotNull();
    }

    @Test
    void loginECadastroGeramAuditoriaComORequestIdESemDadosSensiveis(CapturedOutput output) throws Exception {
        String email = randomEmail();
        String cadastro = mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email))
                        .header("X-Request-Id", "req-cadastro"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID corretor = UUID.fromString(JsonPath.read(cadastro, "$.id"));
        api.confirmar(TestEmail.ultimoToken(email)).andExpect(status().isNoContent());
        String token = api.login(email, SENHA);
        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaErrada1"))
                        .header("X-Request-Id", "req-falha"))
                .andExpect(status().isUnauthorized());

        assertThat(linha(output, "acao=corretor.cadastrado corretor=" + corretor + " recurso=" + corretor))
                .contains("[req-cadastro]");
        assertThat(output).contains("acao=login corretor=" + corretor);
        assertThat(linha(output, "acao=login.falha")).contains("[req-falha]");
        assertThat(output.getAll())
                .doesNotContain(SENHA)
                .doesNotContain("senhaErrada1")
                .doesNotContain(token)
                .doesNotContain("88999990000");
    }

    @Test
    void crudDeClienteEImovelGeraAuditoria(CapturedOutput output) throws Exception {
        String email = randomEmail();
        UUID corretor = api.cadastrar(email);
        String token = api.login(email, SENHA);
        String cpf = randomCpf();

        String clienteBody = """
                {"nome":"Ana","whatsapp":"88988887777","origem":"SITE","cpf":"%s"}
                """.formatted(cpf);
        UUID cliente = criar(token, "/api/clientes", clienteBody);
        mockMvc.perform(comToken(json(put("/api/clientes/" + cliente), clienteBody), token))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(delete("/api/clientes/" + cliente), token))
                .andExpect(status().isNoContent());

        String imovelBody = """
                {"tipo":"CASA","endereco":%s,"precoVenda":100000,
                 "proprietario":{"nome":"Beto","whatsapp":"88977776666","email":"beto@test.com","cpf":"%s"}}
                """.formatted(ENDERECO_PADRAO, randomCpf());
        UUID imovel = criar(token, "/api/imoveis", imovelBody);
        mockMvc.perform(comToken(json(put("/api/imoveis/" + imovel), imovelBody), token))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(delete("/api/imoveis/" + imovel), token))
                .andExpect(status().isNoContent());

        for (String acao : new String[]{"criado", "alterado", "apagado"}) {
            assertThat(output).contains("acao=cliente." + acao + " corretor=" + corretor + " recurso=" + cliente);
            assertThat(output).contains("acao=imovel." + acao + " corretor=" + corretor + " recurso=" + imovel);
        }
        assertThat(output.getAll()).doesNotContain(cpf).doesNotContain("88988887777").doesNotContain("88977776666");
    }

    private UUID criar(String token, String path, String body) throws Exception {
        String resposta = mockMvc.perform(comToken(json(post(path), body), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resposta, "$.id"));
    }

    private static String linha(CapturedOutput output, String trecho) {
        return output.getAll().lines().filter(l -> l.contains(trecho)).findFirst().orElseThrow();
    }
}
