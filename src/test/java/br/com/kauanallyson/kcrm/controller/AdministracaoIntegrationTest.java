package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.config.CriacaoDoAdministrador;
import br.com.kauanallyson.kcrm.repository.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static br.com.kauanallyson.kcrm.TestApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AdministracaoIntegrationTest {
    private static final String CORRETORES = "/api/administracao/corretores";
    private static final String CLIENTE_JSON = """
            {"nome":"Maria","whatsapp":"88999991111","origem":"SITE"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private CriacaoDoAdministrador criacaoDoAdministrador;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    private static String suspensao(UUID corretor) {
        return CORRETORES + "/" + corretor + "/suspensao";
    }

    @Test
    void startupCriaUmUnicoAdministradorMesmoRodandoDeNovo() throws Exception {
        criacaoDoAdministrador.run(new DefaultApplicationArguments());

        assertThat(administradorRepository.count()).isEqualTo(1);
        assertThat(api.loginAdministrador()).isNotBlank();
    }

    @Test
    void administradorListaCorretoresSemSenhaNemCarteira() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email);

        mockMvc.perform(comToken(get(CORRETORES), api.loginAdministrador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].email".formatted(id)).value(hasItem(email)))
                .andExpect(jsonPath("$[?(@.id == '%s')].nome".formatted(id)).value(hasItem("Test")))
                .andExpect(jsonPath("$[?(@.id == '%s')].creci".formatted(id)).value(hasItem("CRECI-CE 1234")))
                .andExpect(jsonPath("$[?(@.id == '%s')].cadastradoEm".formatted(id)).isNotEmpty())
                .andExpect(jsonPath("$[?(@.id == '%s')].suspenso".formatted(id)).value(hasItem(false)))
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(jsonPath("$[0].clientes").doesNotExist());
    }

    @Test
    void corretorNaoAcessaAdministracao() throws Exception {
        String corretor = api.novoCorretor();
        UUID outro = api.cadastrar(randomEmail());

        mockMvc.perform(comToken(get(CORRETORES), corretor))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(put(suspensao(outro)), corretor))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorNaoAcessaCarteira() throws Exception {
        String administrador = api.loginAdministrador();

        mockMvc.perform(comToken(get("/api/clientes"), administrador)).andExpect(status().isForbidden());
        mockMvc.perform(comToken(json(post("/api/clientes"), CLIENTE_JSON), administrador))
                .andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/api/imoveis"), administrador)).andExpect(status().isForbidden());
        mockMvc.perform(comToken(get("/api/clientes/" + UUID.randomUUID()), administrador))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspenderCorretorInexistenteEhNaoEncontrado() throws Exception {
        String administrador = api.loginAdministrador();

        mockMvc.perform(comToken(put(suspensao(UUID.randomUUID())), administrador))
                .andExpect(status().isNotFound());
        mockMvc.perform(comToken(delete(suspensao(UUID.randomUUID())), administrador))
                .andExpect(status().isNotFound());
    }

    @Test
    void suspensaoValeNaHoraEReativacaoDevolveAMesmaCarteira() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email);
        String token = api.login(email, SENHA);
        mockMvc.perform(comToken(json(post("/api/clientes"), CLIENTE_JSON), token))
                .andExpect(status().isCreated());
        String administrador = api.loginAdministrador();

        // Idempotente: suspender duas vezes dá no mesmo
        mockMvc.perform(comToken(put(suspensao(id)), administrador)).andExpect(status().isNoContent());
        mockMvc.perform(comToken(put(suspensao(id)), administrador)).andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/clientes"), token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CONTA_SUSPENSA"));

        mockMvc.perform(comToken(delete(suspensao(id)), administrador)).andExpect(status().isNoContent());
        mockMvc.perform(comToken(delete(suspensao(id)), administrador)).andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/clientes"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Maria"));
    }

    @Test
    void loginDeSuspensoSoRevelaASuspensaoComASenhaCerta() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email);
        mockMvc.perform(comToken(put(suspensao(id)), api.loginAdministrador())).andExpect(status().isNoContent());

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, SENHA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CONTA_SUSPENSA"))
                .andExpect(jsonPath("$.detail").value("Conta suspensa"));
        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, "senhaerrada123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void cadastroComEmailDoAdministradorEhConflito() throws Exception {
        mockMvc.perform(json(post("/api/auth/cadastro"), corretorJson(ADMINISTRADOR_EMAIL)))
                .andExpect(status().isConflict());
    }
}
