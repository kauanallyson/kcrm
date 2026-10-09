package br.com.kauanallyson.kcrm;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Atalhos para os testes de integração falarem com a API como um Corretor
public final class TestApi {
    public static final String SENHA = "senha12345";
    public static final String ADMINISTRADOR_EMAIL = "administrador@kcrm.test";
    public static final String ADMINISTRADOR_SENHA = "administrador123";
    public static final String ENDERECO_PADRAO =
            "{\"rua\":\"Rua A\",\"numero\":\"1\",\"bairro\":\"Centro\",\"cidade\":\"Sobral\",\"estado\":\"CE\",\"cep\":\"62010000\"}";

    private final MockMvc mockMvc;

    public TestApi(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public String login(String email, String senha) throws Exception {
        String body = perform(json(post("/api/auth/login"), loginJson(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    // Cadastra um Corretor pelo auto-cadastro, confirma o e-mail pelo link recebido e devolve o id
    public UUID cadastrar(String email) throws Exception {
        UUID id = cadastrarSemConfirmar(email);
        confirmar(TestEmail.ultimoToken(email)).andExpect(status().isNoContent());
        return id;
    }

    public ResultActions confirmar(String token) throws Exception {
        return perform(json(post("/api/auth/confirmacao"), """
                {"token":"%s"}
                """.formatted(token)));
    }

    public UUID cadastrarSemConfirmar(String email) throws Exception {
        String body = perform(json(post("/api/auth/cadastro"), corretorJson(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    // Cadastra um Corretor novo e devolve o token dele
    public String novoCorretor() throws Exception {
        String email = randomEmail();
        cadastrar(email);
        return login(email, SENHA);
    }

    public String loginAdministrador() throws Exception {
        return login(ADMINISTRADOR_EMAIL, ADMINISTRADOR_SENHA);
    }

    public ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    public static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    public static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", bearer(token));
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    public static String randomEmail() {
        return "corretor-" + UUID.randomUUID() + "@test.com";
    }

    // Gera um CPF com dígitos verificadores válidos
    public static String randomCpf() {
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

    public static String corretorJson(String email) {
        return """
                {"nome":"Test","email":"%s","senha":"%s","creci":"CRECI-CE 1234","whatsapp":"88999990000"}
                """.formatted(email, SENHA);
    }

    public static String loginJson(String email, String senha) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha);
    }
}
