package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.TestApi;
import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.repository.ClienteRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import br.com.kauanallyson.kcrm.repository.ImovelRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class ContaIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CorretorRepository corretorRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ImovelRepository imovelRepository;

    private TestApi api;

    @BeforeEach
    void setUp() {
        api = new TestApi(mockMvc);
    }

    private UUID cadastrarCliente(String token, String cpf) throws Exception {
        String body = """
                {"nome":"Maria","whatsapp":"88999991111","origem":"SITE","cpf":"%s"}
                """.formatted(cpf);
        String resposta = mockMvc.perform(comToken(json(post("/api/clientes"), body), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resposta, "$.id"));
    }

    private UUID cadastrarImovel(String token, String cpfProprietario) throws Exception {
        String body = """
                {"tipo":"CASA","endereco":%s,"precoVenda":250000,
                 "proprietario":{"nome":"Ana","whatsapp":"88999991111","email":"ana@test.com","cpf":"%s"}}
                """.formatted(ENDERECO_PADRAO, cpfProprietario);
        String resposta = mockMvc.perform(comToken(json(post("/api/imoveis"), body), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resposta, "$.id"));
    }

    private static String senhaJson(String senha) {
        return """
                {"senha":"%s"}
                """.formatted(senha);
    }

    @Test
    void encerramentoApagaCorretorECarteiraSemTocarEmOutros() throws Exception {
        String email = randomEmail();
        UUID corretorId = api.cadastrar(email);
        String token = api.login(email, SENHA);
        UUID cliente = cadastrarCliente(token, randomCpf());
        UUID imovel = cadastrarImovel(token, randomCpf());

        String outro = api.novoCorretor();
        UUID clienteDoOutro = cadastrarCliente(outro, randomCpf());
        UUID imovelDoOutro = cadastrarImovel(outro, randomCpf());

        mockMvc.perform(comToken(json(post("/api/conta/encerramento"), senhaJson(SENHA)), token))
                .andExpect(status().isNoContent());

        assertThat(corretorRepository.existsById(corretorId)).isFalse();
        assertThat(clienteRepository.existsById(cliente)).isFalse();
        assertThat(imovelRepository.existsById(imovel)).isFalse();
        assertThat(clienteRepository.existsById(clienteDoOutro)).isTrue();
        assertThat(imovelRepository.existsById(imovelDoOutro)).isTrue();
    }

    @Test
    void senhaErradaNaoApagaNada() throws Exception {
        String email = randomEmail();
        UUID corretorId = api.cadastrar(email);
        String token = api.login(email, SENHA);
        UUID cliente = cadastrarCliente(token, randomCpf());

        mockMvc.perform(comToken(json(post("/api/conta/encerramento"), senhaJson("errada123")), token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SENHA_INCORRETA"));

        assertThat(corretorRepository.existsById(corretorId)).isTrue();
        assertThat(clienteRepository.existsById(cliente)).isTrue();
    }

    @Test
    void depoisDoEncerramentoTokenAntigoNaoValeEEmailAceitaNovoCadastroVazio() throws Exception {
        String email = randomEmail();
        api.cadastrar(email);
        String token = api.login(email, SENHA);
        cadastrarCliente(token, randomCpf());

        mockMvc.perform(comToken(json(post("/api/conta/encerramento"), senhaJson(SENHA)), token))
                .andExpect(status().isNoContent());

        mockMvc.perform(comToken(get("/api/clientes"), token))
                .andExpect(status().isUnauthorized());

        api.cadastrar(email);
        String novoToken = api.login(email, SENHA);
        mockMvc.perform(comToken(get("/api/conta/exportacao"), novoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientes").isEmpty())
                .andExpect(jsonPath("$.imoveis").isEmpty());
    }

    @Test
    void exportacaoTrazACarteiraCompletaSemSenhaENadaDeOutroCorretor() throws Exception {
        String email = randomEmail();
        api.cadastrar(email);
        String token = api.login(email, SENHA);
        String cpfCliente = randomCpf();
        cadastrarCliente(token, cpfCliente);
        cadastrarImovel(token, randomCpf());

        String outro = api.novoCorretor();
        cadastrarCliente(outro, randomCpf());

        String resposta = mockMvc.perform(comToken(get("/api/conta/exportacao"), token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("carteira.json")))
                .andExpect(jsonPath("$.corretor.email").value(email))
                .andExpect(jsonPath("$.clientes.length()").value(1))
                .andExpect(jsonPath("$.imoveis.length()").value(1))
                .andExpect(jsonPath("$.imoveis[0].proprietario.cpf").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        assertThat(resposta).doesNotContain(SENHA).doesNotContainIgnoringCase("senha");
        assertThat((String) JsonPath.read(resposta, "$.clientes[0].cpf")).isNotEmpty();
    }

    @Test
    void cpfNuncaApareceNoLog(CapturedOutput output) throws Exception {
        String token = api.novoCorretor();
        String cpfCliente = randomCpf();
        String cpfProprietario = randomCpf();
        UUID cliente = cadastrarCliente(token, cpfCliente);
        cadastrarImovel(token, cpfProprietario);

        // Erro de validação também passa pelo log; o CPF da requisição não pode vazar por ele
        mockMvc.perform(comToken(json(put("/api/clientes/" + cliente),
                        "{\"nome\":\"\",\"whatsapp\":\"1\",\"origem\":\"SITE\",\"cpf\":\"" + cpfCliente + "\"}"), token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(comToken(get("/api/conta/exportacao"), token)).andExpect(status().isOk());

        for (String cpf : new String[]{cpfCliente, cpfProprietario}) {
            String formatado = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
            assertThat(output.getAll()).doesNotContain(cpf).doesNotContain(formatado);
        }
    }
}
