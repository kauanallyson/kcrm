package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import br.com.kauanallyson.kcrm.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static br.com.kauanallyson.kcrm.TestApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class UsuarioIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private TestApi api;
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        api = new TestApi(mockMvc);
        admin = api.loginAdminInicial();
    }

    @Test
    void adminCadastraCorretor() throws Exception {
        String email = randomEmail();

        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(email, randomCpf(), "CORRETOR")), admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.perfil").value("CORRETOR"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario salvo = usuarioRepository.findByEmail(new Email(email)).orElseThrow();
        assertThat(salvo.getSenhaHash().value()).startsWith("{bcrypt}").doesNotContain(SENHA);
        api.login(email, SENHA);
    }

    @Test
    void adminCadastraOutroAdmin() throws Exception {
        String email = randomEmail();
        api.cadastrar(email, "ADMIN");

        mockMvc.perform(comToken(get("/api/usuarios"), api.login(email, SENHA)))
                .andExpect(status().isOk());
    }

    @Test
    void cadastroExigePerfil() throws Exception {
        String semPerfil = usuarioJson(randomEmail(), randomCpf());

        mockMvc.perform(comToken(json(post("/api/usuarios"), semPerfil), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.errors.perfil").exists());
    }

    @Test
    void cadastroComEmailOuCpfDuplicadoEhConflito() throws Exception {
        String email = randomEmail();
        String cpf = randomCpf();
        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(email, cpf, "CORRETOR")), admin))
                .andExpect(status().isCreated());

        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(email, randomCpf(), "CORRETOR")), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_EXISTE"));
        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(randomEmail(), cpf, "CORRETOR")), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_EXISTE"));
    }

    @Test
    void cpfEhGuardadoFormatadoEMesmoCpfComOutraGrafiaEhConflito() throws Exception {
        String cpf = randomCpf();
        String formatado = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9)
                + "-" + cpf.substring(9);
        String email = randomEmail();

        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(email, cpf, "CORRETOR")), admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(formatado));
        assertThat(usuarioRepository.findByEmail(new Email(email)).orElseThrow().getCpf().value()).isEqualTo(formatado);

        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson(randomEmail(), formatado, "CORRETOR")), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_EXISTE"));
    }

    @Test
    void camposInvalidosRetornamErrosDeValidacao() throws Exception {
        mockMvc.perform(comToken(json(post("/api/usuarios"), cadastroJson("not-an-email", "123", "CORRETOR")), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.cpf").exists());
    }

    @Test
    void telefoneEhGuardadoEDevolvidoFormatado() throws Exception {
        String email = randomEmail();
        String body = cadastroJson(email, randomCpf(), "CORRETOR").replace("\"88999990000\"", "\"88 98888 7777\"");

        mockMvc.perform(comToken(json(post("/api/usuarios"), body), admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telefone").value("(88) 98888-7777"));
        assertThat(usuarioRepository.findByEmail(new Email(email)).orElseThrow().getTelefone().value())
                .isEqualTo("(88) 98888-7777");
    }

    @Test
    void telefoneFixoEhRejeitado() throws Exception {
        String body = cadastroJson(randomEmail(), randomCpf(), "CORRETOR").replace("\"88999990000\"", "\"(88) 3611-0000\"");

        mockMvc.perform(comToken(json(post("/api/usuarios"), body), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.telefone").exists());
    }

    @Test
    void enderecoEhUmObjetoAninhadoQueIdaEVolta() throws Exception {
        String endereco = """
                {"rua":" Rua das Flores ","numero":"S/N","complemento":" Casa 2 ","bairro":"Centro",
                "cidade":"Sobral","estado":"ce","cep":"62.010000"}""";
        String body = cadastroJson(randomEmail(), randomCpf(), "CORRETOR").replace(ENDERECO_PADRAO, endereco);

        String location = mockMvc.perform(comToken(json(post("/api/usuarios"), body), admin))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(comToken(get(location), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco.rua").value("Rua das Flores"))
                .andExpect(jsonPath("$.endereco.numero").value("S/N"))
                .andExpect(jsonPath("$.endereco.complemento").value("Casa 2"))
                .andExpect(jsonPath("$.endereco.bairro").value("Centro"))
                .andExpect(jsonPath("$.endereco.cidade").value("Sobral"))
                .andExpect(jsonPath("$.endereco.estado").value("CE"))
                .andExpect(jsonPath("$.endereco.cep").value("62010-000"));
    }

    @Test
    void enderecoSemComplementoDevolveComplementoNulo() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");

        mockMvc.perform(comToken(get("/api/usuarios/" + id), admin))
                .andExpect(jsonPath("$.endereco.complemento").doesNotExist())
                .andExpect(jsonPath("$.endereco.cep").value("62010-000"));
    }

    @Test
    void enderecoInvalidoRetornaErroPorCampo() throws Exception {
        String endereco = """
                {"rua":" ","numero":"1","bairro":"Centro","cidade":"Sobral","estado":"XX","cep":"123"}""";
        String body = cadastroJson(randomEmail(), randomCpf(), "CORRETOR").replace(ENDERECO_PADRAO, endereco);

        mockMvc.perform(comToken(json(post("/api/usuarios"), body), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.errors['endereco.rua']").exists())
                .andExpect(jsonPath("$.errors['endereco.estado']").exists())
                .andExpect(jsonPath("$.errors['endereco.cep']").exists())
                .andExpect(jsonPath("$.errors['endereco.numero']").doesNotExist());
    }

    @Test
    void cadastroSemEnderecoEhRejeitado() throws Exception {
        String body = cadastroJson(randomEmail(), randomCpf(), "CORRETOR").replace("\"endereco\":" + ENDERECO_PADRAO + ",", "");

        mockMvc.perform(comToken(json(post("/api/usuarios"), body), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endereco").exists());
    }

    @Test
    void adminListaVeEEditaQualquerUsuario() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");

        mockMvc.perform(comToken(get("/api/usuarios"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).exists());
        mockMvc.perform(comToken(get("/api/usuarios/" + id), admin))
                .andExpect(status().isOk());
        String body = usuarioJson(email, randomCpf()).replace("\"Test\"", "\"Novo Nome\"");
        mockMvc.perform(comToken(json(put("/api/usuarios/" + id), body), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"));
    }

    @Test
    void corretorSoVeEEditaASiMesmo() throws Exception {
        String emailA = randomEmail();
        UUID idA = api.cadastrar(emailA, "CORRETOR");
        UUID idB = api.cadastrar(randomEmail(), "CORRETOR");
        String tokenA = api.login(emailA, SENHA);

        mockMvc.perform(comToken(get("/api/usuarios/" + idA), tokenA))
                .andExpect(status().isOk());
        mockMvc.perform(comToken(json(put("/api/usuarios/" + idA), usuarioJson(emailA, randomCpf())), tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(get("/api/usuarios/" + idB), tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));
        mockMvc.perform(comToken(json(put("/api/usuarios/" + idB), usuarioJson(randomEmail(), randomCpf())), tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    void corretorNaoCadastraNaoListaNaoMudaPerfilNemDesativa() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");
        UUID outro = api.cadastrar(randomEmail(), "CORRETOR");
        String corretor = api.login(email, SENHA);

        esperarAcessoNegado(mockMvc.perform(comToken(
                json(post("/api/usuarios"), cadastroJson(randomEmail(), randomCpf(), "CORRETOR")), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(get("/api/usuarios"), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(
                json(patch("/api/usuarios/" + id + "/perfil"), "{\"perfil\":\"ADMIN\"}"), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(
                json(post("/api/usuarios/" + outro + "/desativacao"), "{\"motivo\":\"x\"}"), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(
                json(post("/api/usuarios/" + id + "/desativacao"), "{\"motivo\":\"x\"}"), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(post("/api/usuarios/" + outro + "/reativacao"), corretor)));
        esperarAcessoNegado(mockMvc.perform(comToken(get("/api/usuarios/" + id + "/historico"), corretor)));

        assertThat(usuarioRepository.findById(id).orElseThrow().getPerfil()).isEqualTo(Perfil.CORRETOR);
    }

    @Test
    void adminMudaPerfil() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");

        mockMvc.perform(comToken(json(patch("/api/usuarios/" + id + "/perfil"), "{\"perfil\":\"ADMIN\"}"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
        mockMvc.perform(comToken(get("/api/usuarios"), api.login(email, SENHA)))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(json(patch("/api/usuarios/" + id + "/perfil"), "{\"perfil\":\"CORRETOR\"}"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("CORRETOR"));
    }

    @Test
    void usuarioDesativadoNaoLogaETokenAntigoEhRejeitado() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");
        String tokenAntigo = api.login(email, SENHA);

        desativar(id, "Saiu da imobiliária")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(json(post("/api/auth/login"), loginJson(email, SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIAIS_INVALIDAS"));
        mockMvc.perform(comToken(get("/api/usuarios/" + id), tokenAntigo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NAO_AUTENTICADO"));
        assertThat(usuarioRepository.findById(id)).isPresent();
    }

    @Test
    void reativacaoDevolveOAcesso() throws Exception {
        String email = randomEmail();
        UUID id = api.cadastrar(email, "CORRETOR");
        String tokenAntigo = api.login(email, SENHA);
        desativar(id, "Férias longas").andExpect(status().isOk());

        mockMvc.perform(comToken(json(post("/api/usuarios/" + id + "/reativacao"), "{\"motivo\":\"Voltou\"}"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));

        mockMvc.perform(comToken(get("/api/usuarios/" + id), tokenAntigo))
                .andExpect(status().isOk());
        api.login(email, SENHA);
    }

    @Test
    void desativacaoExigeMotivo() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");

        desativar(id, " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.motivo").exists());
        mockMvc.perform(comToken(json(post("/api/usuarios/" + id + "/desativacao"), "{}"), admin))
                .andExpect(status().isBadRequest());
        assertThat(usuarioRepository.findById(id).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void desativarDuasVezesOuReativarAtivoEhConflito() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");

        mockMvc.perform(comToken(post("/api/usuarios/" + id + "/reativacao"), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_ATIVO"));
        desativar(id, "motivo").andExpect(status().isOk());
        desativar(id, "motivo")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USUARIO_JA_DESATIVADO"));
    }

    @Test
    void historicoMostraEventosDoUsuarioDoMaisRecenteAoMaisAntigo() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");
        UUID outro = api.cadastrar(randomEmail(), "CORRETOR");
        Usuario adminInicial = usuarioRepository.findByEmail(new Email(ADMIN_EMAIL)).orElseThrow();

        desativar(id, "  Primeira saída ").andExpect(status().isOk());
        mockMvc.perform(comToken(post("/api/usuarios/" + id + "/reativacao"), admin)).andExpect(status().isOk());
        mudarPerfil(id, "ADMIN").andExpect(status().isOk());
        desativar(id, "Segunda saída").andExpect(status().isOk());
        desativar(outro, "Evento de outro Usuário").andExpect(status().isOk());

        mockMvc.perform(comToken(get("/api/usuarios/" + id + "/historico"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].tipo").value("DESATIVACAO"))
                .andExpect(jsonPath("$[0].motivo").value("Segunda saída"))
                .andExpect(jsonPath("$[0].detalhe").doesNotExist())
                .andExpect(jsonPath("$[0].autorId").value(adminInicial.getId().toString()))
                .andExpect(jsonPath("$[0].autorNome").value(adminInicial.getNome()))
                .andExpect(jsonPath("$[0].ocorridoEm").isNotEmpty())
                .andExpect(jsonPath("$[1].tipo").value("MUDANCA_DE_PERFIL"))
                .andExpect(jsonPath("$[1].motivo").doesNotExist())
                .andExpect(jsonPath("$[1].detalhe").value("CORRETOR -> ADMIN"))
                .andExpect(jsonPath("$[2].tipo").value("REATIVACAO"))
                .andExpect(jsonPath("$[2].motivo").doesNotExist())
                .andExpect(jsonPath("$[3].tipo").value("DESATIVACAO"))
                .andExpect(jsonPath("$[3].motivo").value("Primeira saída"));
    }

    @Test
    void reativacaoComMotivoRegistraOMotivo() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");
        desativar(id, "Férias").andExpect(status().isOk());
        mockMvc.perform(comToken(json(post("/api/usuarios/" + id + "/reativacao"), "{\"motivo\":\"Voltou\"}"), admin))
                .andExpect(status().isOk());

        mockMvc.perform(comToken(get("/api/usuarios/" + id + "/historico"), admin))
                .andExpect(jsonPath("$[0].tipo").value("REATIVACAO"))
                .andExpect(jsonPath("$[0].motivo").value("Voltou"));
    }

    @Test
    void mudarParaOMesmoPerfilNaoRegistraEvento() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");

        mudarPerfil(id, "CORRETOR").andExpect(status().isOk());

        mockMvc.perform(comToken(get("/api/usuarios/" + id + "/historico"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historicoDeUsuarioInexistenteEhNaoEncontrado() throws Exception {
        mockMvc.perform(comToken(get("/api/usuarios/" + UUID.randomUUID() + "/historico"), admin))
                .andExpect(status().isNotFound());
    }

    private ResultActions mudarPerfil(UUID id, String perfil) throws Exception {
        return mockMvc.perform(comToken(json(patch("/api/usuarios/" + id + "/perfil"),
                "{\"perfil\":\"" + perfil + "\"}"), admin));
    }

    @Test
    void adminPodeDesativarOutroAdminESiMesmoSeNaoForOUltimo() throws Exception {
        String email = randomEmail();
        UUID outroAdmin = api.cadastrar(email, "ADMIN");
        String tokenOutro = api.login(email, SENHA);

        mockMvc.perform(comToken(json(post("/api/usuarios/" + outroAdmin + "/desativacao"),
                        "{\"motivo\":\"Se desligou\"}"), tokenOutro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(comToken(get("/api/usuarios"), tokenOutro))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ultimoAdminAtivoNaoPodeSerRebaixadoNemDesativado() throws Exception {
        // Desativa os Admins criados por outros testes, deixando só o Admin inicial
        UUID adminInicial = usuarioRepository.findByEmail(new Email(ADMIN_EMAIL)).orElseThrow().getId();
        for (Usuario outro : usuarioRepository.findAllByPerfilAndAtivoTrue(Perfil.ADMIN)) {
            if (!outro.getId().equals(adminInicial)) {
                desativar(outro.getId(), "limpeza do teste").andExpect(status().isOk());
            }
        }

        mockMvc.perform(comToken(json(patch("/api/usuarios/" + adminInicial + "/perfil"),
                        "{\"perfil\":\"CORRETOR\"}"), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ULTIMO_ADMIN"));
        desativar(adminInicial, "tentativa")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ULTIMO_ADMIN"));

        Usuario depois = usuarioRepository.findById(adminInicial).orElseThrow();
        assertThat(depois.isAtivo()).isTrue();
        assertThat(depois.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    void naoHaMaisExclusaoDeUsuario() throws Exception {
        UUID id = api.cadastrar(randomEmail(), "CORRETOR");

        mockMvc.perform(comToken(delete("/api/usuarios/" + id), admin))
                .andExpect(status().isMethodNotAllowed());
        assertThat(usuarioRepository.findById(id)).isPresent();
    }

    private ResultActions desativar(UUID id, String motivo) throws Exception {
        return mockMvc.perform(comToken(json(post("/api/usuarios/" + id + "/desativacao"),
                "{\"motivo\":\"%s\"}".formatted(motivo)), admin));
    }

    private static void esperarAcessoNegado(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACESSO_NEGADO"));
    }
}
