package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.model.Email;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    @Test
    void adminInicialEhCriadoNaSubidaComSenhaEmHash() {
        var admin = usuarioRepository.findByEmail(new Email(ADMIN_EMAIL)).orElseThrow();

        assertThat(admin.getPerfil().name()).isEqualTo("ADMIN");
        assertThat(admin.getSenhaHash().value()).startsWith("{bcrypt}").doesNotContain(ADMIN_SENHA);
    }

    @Test
    void cadastroPublicoNaoExisteMais() throws Exception {
        mockMvc.perform(json(post("/api/auth/register"), usuarioJson(randomEmail(), randomCpf())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        String email = randomEmail();
        api.cadastrar(email, "CORRETOR");

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void loginComSenhaErradaOuEmailDesconhecidoEhNaoAutorizadoComMesmaMensagem() throws Exception {
        String email = randomEmail();
        api.cadastrar(email, "CORRETOR");

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaerrada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
        mockMvc.perform(json(post("/api/auth/login"), loginJson(randomEmail(), SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void meRetornaOUsuarioLogado() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");
        String token = api.login(email, SENHA);

        mockMvc.perform(comToken(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.perfil").value("CORRETOR"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void meSemTokenEhNaoAutorizado() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NAO_AUTENTICADO"));
    }

    @Test
    void rotaProtegidaSemTokenEhNaoAutorizada() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NAO_AUTENTICADO"));
    }

    @Test
    void rotaProtegidaComTokenInvalidoEhNaoAutorizada() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + UUID.randomUUID()).header("Authorization", "Bearer abc.def.ghi"))
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
        mockMvc.perform(comToken(get("/api/usuarios/not-a-uuid"), api.loginAdminInicial()))
                .andExpect(status().isBadRequest());
    }
}
