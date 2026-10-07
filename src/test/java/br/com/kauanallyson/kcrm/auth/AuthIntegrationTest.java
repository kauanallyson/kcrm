package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.model.Email;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthIntegrationTest {
    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void cadastroRetornaTokenEGuardaHashDaSenha() throws Exception {
        String email = randomEmail();

        postJson("/api/auth/register", usuarioJson(email, randomCpf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));

        String stored = usuarioRepository.findByEmail(new Email(email)).orElseThrow().getSenhaHash().value();
        assertThat(stored).startsWith("{bcrypt}").doesNotContain(SENHA);
    }

    @Test
    void cadastroComEmailDuplicadoEhConflito() throws Exception {
        String email = randomEmail();
        cadastrar(email);

        postJson("/api/auth/register", usuarioJson(email, randomCpf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_EXISTE"));
    }

    @Test
    void cadastroComCpfDuplicadoEhConflito() throws Exception {
        String cpf = randomCpf();
        cadastrar(randomEmail(), cpf);

        postJson("/api/auth/register", usuarioJson(randomEmail(), cpf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_EXISTE"));
    }

    @Test
    void loginComCredenciaisValidasRetornaToken() throws Exception {
        String email = randomEmail();
        cadastrar(email);

        postJson("/api/auth/login", loginJson(email, SENHA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginComSenhaErradaOuEmailDesconhecidoEhNaoAutorizadoComMesmaMensagem() throws Exception {
        String email = randomEmail();
        cadastrar(email);

        postJson("/api/auth/login", loginJson(email, "senhaerrada123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
        postJson("/api/auth/login", loginJson(randomEmail(), SENHA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void meRetornaOUsuarioLogado() throws Exception {
        String email = randomEmail();
        String token = cadastrar(email);

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idOf(email).toString()))
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
    void usuarioSoAcessaASiMesmo() throws Exception {
        String emailA = randomEmail();
        String cpfA = randomCpf();
        String tokenA = cadastrar(emailA, cpfA);
        String emailB = randomEmail();
        cadastrar(emailB);
        UUID idA = idOf(emailA);
        UUID idB = idOf(emailB);

        mockMvc.perform(get("/api/usuarios/" + idA).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());
        mockMvc.perform(json(put("/api/usuarios/" + idA), usuarioJson(emailA, cpfA)).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usuarios/" + idB).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(put("/api/usuarios/" + idB), usuarioJson(emailB, randomCpf())).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/usuarios/" + idB).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));
    }

    @Test
    void usuarioPodeSeExcluirESeuTokenDeixaDeFuncionar() throws Exception {
        String email = randomEmail();
        String token = cadastrar(email);
        UUID id = idOf(email);

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotasDeListarECriarUsuarioNaoExistem() throws Exception {
        String token = cadastrar(randomEmail());

        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(post("/api/usuarios"), usuarioJson(randomEmail(), randomCpf())).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void jsonMalformadoEhRequisicaoInvalida() throws Exception {
        postJson("/api/auth/login", "{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void camposInvalidosRetornamErrosDeValidacao() throws Exception {
        postJson("/api/auth/register", usuarioJson("not-an-email", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.cpf").exists());
    }

    @Test
    void idInvalidoEhRequisicaoInvalida() throws Exception {
        String token = cadastrar(randomEmail());

        mockMvc.perform(get("/api/usuarios/not-a-uuid").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private String cadastrar(String email) throws Exception {
        return cadastrar(email, randomCpf());
    }

    private String cadastrar(String email, String cpf) throws Exception {
        String body = postJson("/api/auth/register", usuarioJson(email, cpf))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(json(post(url), body));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private UUID idOf(String email) {
        return usuarioRepository.findByEmail(new Email(email)).orElseThrow().getId();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String randomEmail() {
        return "usuario-" + UUID.randomUUID() + "@test.com";
    }

    // Gera um CPF com dígitos verificadores válidos para passar na validação @CPF
    private static String randomCpf() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        for (int pos = 9; pos < 11; pos++) {
            int sum = 0;
            for (int i = 0; i < pos; i++) {
                sum += d[i] * (pos + 1 - i);
            }
            int check = 11 - sum % 11;
            d[pos] = check >= 10 ? 0 : check;
        }
        StringBuilder cpf = new StringBuilder();
        for (int digit : d) {
            cpf.append(digit);
        }
        return cpf.toString();
    }

    private static String usuarioJson(String email, String cpf) {
        return """
                {"nome":"Test","cpf":"%s","email":"%s","senha":"%s","telefone":"1","endereco":"a"}
                """.formatted(cpf, email, SENHA);
    }

    private static String loginJson(String email, String senha) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha);
    }
}
