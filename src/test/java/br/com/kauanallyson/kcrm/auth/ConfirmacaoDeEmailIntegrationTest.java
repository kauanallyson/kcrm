package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestEmail;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.service.ConfirmacaoDeEmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
class ConfirmacaoDeEmailIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorretorRepository corretorRepository;

    @Autowired
    private ConfirmacaoDeEmailService confirmacaoDeEmail;

    @Autowired
    private JdbcTemplate jdbc;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    private static String reenvioJson(String email) {
        return """
                {"email":"%s"}
                """.formatted(email);
    }

    private boolean confirmado(String email) {
        return corretorRepository.findByEmail(new Email(email)).orElseThrow().isEmailConfirmado();
    }

    @Test
    void cadastroEnviaEmailEmPortuguesComLink() throws Exception {
        String email = randomEmail();
        api.cadastrarSemConfirmar(email);

        List<MimeMessage> mensagens = TestEmail.recebidos(email);
        assertThat(mensagens).hasSize(1);
        assertThat(mensagens.getFirst().getSubject()).isEqualTo("Confirme seu e-mail no kcrm");
        assertThat(TestEmail.texto(mensagens.getFirst()))
                .contains("confirme com a senha", "1 hora", "http://localhost:5173/confirmar-email?token=");
        assertThat(confirmado(email)).isFalse();
    }

    @Test
    void linkValidoConfirmaEUmaUnicaVez() throws Exception {
        String email = randomEmail();
        api.cadastrarSemConfirmar(email);
        String token = TestEmail.ultimoToken(email);

        api.confirmar(token).andExpect(status().isNoContent());
        assertThat(confirmado(email)).isTrue();
        assertThat(api.login(email, SENHA)).isNotBlank();

        api.confirmar(token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LINK_DE_CONFIRMACAO_INVALIDO"));
    }

    @Test
    void linkExpiradoOuInexistenteNaoConfirma() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrarSemConfirmar(email);
        jdbc.update("update confirmacoes_email set expira_em = now() - interval '1 minute' where corretor_id = ?", id);

        api.confirmar(TestEmail.ultimoToken(email))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LINK_DE_CONFIRMACAO_INVALIDO"));
        api.confirmar("token-que-nao-existe")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LINK_DE_CONFIRMACAO_INVALIDO"));
        assertThat(confirmado(email)).isFalse();
    }

    @Test
    void loginDeNaoConfirmadoSoRevelaComASenhaCerta() throws Exception {
        String email = randomEmail();
        api.cadastrarSemConfirmar(email);

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, SENHA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NAO_CONFIRMADO"))
                .andExpect(jsonPath("$.detail").value("E-mail não confirmado"));
        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaerrada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void reenvioInvalidaOLinkAnteriorENaoRevelaOEmail() throws Exception {
        String email = randomEmail();
        api.cadastrarSemConfirmar(email);
        String antigo = TestEmail.ultimoToken(email);

        mockMvc.perform(json(post("/api/auth/confirmacao/reenvio"), reenvioJson(email)))
                .andExpect(status().isAccepted());
        String novo = TestEmail.ultimoToken(email);
        assertThat(novo).isNotEqualTo(antigo);
        assertThat(TestEmail.recebidos(email)).hasSize(2);

        api.confirmar(antigo).andExpect(status().isBadRequest());
        api.confirmar(novo).andExpect(status().isNoContent());

        // Desconhecido, já confirmado ou malformado: mesma resposta, nenhum e-mail
        String desconhecido = randomEmail();
        mockMvc.perform(json(post("/api/auth/confirmacao/reenvio"), reenvioJson(desconhecido)))
                .andExpect(status().isAccepted());
        mockMvc.perform(json(post("/api/auth/confirmacao/reenvio"), reenvioJson(email)))
                .andExpect(status().isAccepted());
        mockMvc.perform(json(post("/api/auth/confirmacao/reenvio"), reenvioJson("nao-eh-email")))
                .andExpect(status().isAccepted());
        assertThat(TestEmail.recebidos(desconhecido)).isEmpty();
        assertThat(TestEmail.recebidos(email)).hasSize(2);
    }

    @Test
    void recadastroComEmailNaoConfirmadoTrocaOsDadosEReenviaOLink() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrarSemConfirmar(email);
        String antigo = TestEmail.ultimoToken(email);

        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isAccepted());

        assertThat(TestEmail.recebidos(email)).hasSize(2);
        api.confirmar(antigo).andExpect(status().isBadRequest());
        api.confirmar(TestEmail.ultimoToken(email)).andExpect(status().isNoContent());
        assertThat(corretorRepository.findByEmail(new Email(email)).orElseThrow().getId()).isEqualTo(id);

        // Confirmado, volta ao comportamento de sempre
        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CORRETOR_JA_EXISTE"));
    }

    // Alguém cadastra antes o e-mail de outra pessoa: o dono, ao se cadastrar, fica com a conta e a própria senha
    @Test
    void quemCadastrouOEmailDeOutraPessoaNaoFicaComAConta() throws Exception {
        String email = randomEmail();
        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email).replace(SENHA, "senhaDoIntruso1")))
                .andExpect(status().isCreated());
        String linkDoIntruso = TestEmail.ultimoToken(email);

        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isAccepted());
        api.confirmar(TestEmail.ultimoToken(email), SENHA).andExpect(status().isNoContent());

        assertThat(api.login(email, SENHA)).isNotBlank();
        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaDoIntruso1")))
                .andExpect(status().isUnauthorized());
        api.confirmar(linkDoIntruso, "senhaDoIntruso1").andExpect(status().isBadRequest());
    }

    // O dono abre um link disparado por outra pessoa: sem a senha dela, a conta não é confirmada
    @Test
    void linkSoConfirmaComASenhaDoCadastro() throws Exception {
        String email = randomEmail();
        api.cadastrarSemConfirmar(email);
        String token = TestEmail.ultimoToken(email);

        api.confirmar(token, "outraSenha123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
        assertThat(confirmado(email)).isFalse();

        // Senha errada não gasta o link
        api.confirmar(token, SENHA).andExpect(status().isNoContent());
        assertThat(confirmado(email)).isTrue();
    }

    @Test
    void tokenDeContaNaoConfirmadaNaoDaAcesso() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email);
        String token = api.login(email, SENHA);
        jdbc.update("update corretores set email_confirmado = false where id = ?", id);

        mockMvc.perform(comToken(get("/api/clientes"), token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NAO_CONFIRMADO"));
    }

    @Test
    void jobApagaSoContasNaoConfirmadasComMaisDe7Dias() throws Exception {
        String antigaNaoConfirmada = randomEmail();
        String recenteNaoConfirmada = randomEmail();
        String antigaConfirmada = randomEmail();
        UUID idAntiga = api.cadastrarSemConfirmar(antigaNaoConfirmada);
        api.cadastrarSemConfirmar(recenteNaoConfirmada);
        UUID idConfirmada = api.cadastrar(antigaConfirmada);
        jdbc.update("update corretores set criado_em = now() - interval '8 days' where id in (?, ?)",
                idAntiga, idConfirmada);

        assertThat(confirmacaoDeEmail.apagarNaoConfirmadas()).isGreaterThanOrEqualTo(1);

        assertThat(corretorRepository.findByEmail(new Email(antigaNaoConfirmada))).isEmpty();
        assertThat(corretorRepository.findByEmail(new Email(recenteNaoConfirmada))).isPresent();
        assertThat(corretorRepository.findByEmail(new Email(antigaConfirmada))).isPresent();
        // O e-mail fica livre para um novo cadastro
        api.cadastrarSemConfirmar(antigaNaoConfirmada);
    }

    @Test
    void administradorNaoPassaPorConfirmacao() throws Exception {
        assertThat(api.loginAdministrador()).isNotBlank();
    }
}
