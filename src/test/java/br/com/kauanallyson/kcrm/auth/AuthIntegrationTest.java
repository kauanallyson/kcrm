package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static br.com.kauanallyson.kcrm.TestApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorretorRepository corretorRepository;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    @Test
    void cadastroPublicoCriaCorretorComSenhaEmHash() throws Exception {
        String email = randomEmail();

        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.creci").value("CRECI-CE 1234"))
                .andExpect(jsonPath("$.whatsapp").value("(88) 99999-0000"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        var corretor = corretorRepository.findByEmail(new Email(email)).orElseThrow();
        assertThat(corretor.getSenhaHash().value()).startsWith("{bcrypt}").doesNotContain(SENHA);
    }

    @Test
    void cadastroComEmailRepetidoEhConflito() throws Exception {
        String email = randomEmail();
        api.cadastrar(email);

        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CORRETOR_JA_EXISTE"));
    }

    @Test
    void cadastroComCamposInvalidosRetornaErrosPorCampo() throws Exception {
        String body = """
                {"nome":"Test","email":"x","senha":"curta","creci":" ","whatsapp":"123"}
                """;
        mockMvc.perform(json(post("/api/auth/cadastro"), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.senha").exists())
                .andExpect(jsonPath("$.errors.creci").exists());
    }

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        String email = randomEmail();
        api.cadastrar(email);

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void loginComSenhaErradaOuEmailDesconhecidoEhNaoAutorizadoComMesmaMensagem() throws Exception {
        String email = randomEmail();
        api.cadastrar(email);

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaerrada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
        mockMvc.perform(json(post("/api/auth/login"), loginJson(randomEmail(), SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void meRetornaOCorretorLogado() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email);
        String token = api.login(email, SENHA);

        mockMvc.perform(comToken(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void meSemTokenEhNaoAutorizado() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NAO_AUTENTICADO"));
    }

    @Test
    void rotaProtegidaComTokenInvalidoEhNaoAutorizada() throws Exception {
        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jsonMalformadoEhRequisicaoInvalida() throws Exception {
        mockMvc.perform(json(post("/api/auth/login"), "{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void idInvalidoEhRequisicaoInvalida() throws Exception {
        mockMvc.perform(comToken(get("/api/clientes/not-a-uuid"), api.novoCorretor()))
                .andExpect(status().isBadRequest());
    }
}
