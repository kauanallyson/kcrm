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

// Atalhos para os testes de integração falarem com a API como Admin ou Corretor
public final class TestApi {
    public static final String ADMIN_EMAIL = "admin-inicial@test.com";
    public static final String ADMIN_SENHA = "admin12345";
    public static final String SENHA = "senha12345";

    private final MockMvc mockMvc;

    public TestApi(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public String loginAdminInicial() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_SENHA);
    }

    public String login(String email, String senha) throws Exception {
        String body = perform(json(post("/api/auth/login"), loginJson(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    // Cadastra pelo Admin inicial e devolve o id
    public UUID cadastrar(String email, String perfil) throws Exception {
        String body = perform(json(post("/api/usuarios"), cadastroJson(email, randomCpf(), perfil))
                .header("Authorization", bearer(loginAdminInicial())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
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
        return "usuario-" + UUID.randomUUID() + "@test.com";
    }

    // Gera um CPF com dígitos verificadores válidos para passar na validação @CPF
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

    public static String usuarioJson(String email, String cpf) {
        return """
                {"nome":"Test","cpf":"%s","email":"%s","senha":"%s","telefone":"88999990000","endereco":"a"}
                """.formatted(cpf, email, SENHA);
    }

    public static String cadastroJson(String email, String cpf, String perfil) {
        return """
                {"nome":"Test","cpf":"%s","email":"%s","senha":"%s","telefone":"88999990000","endereco":"a","perfil":"%s"}
                """.formatted(cpf, email, SENHA, perfil);
    }

    public static String loginJson(String email, String senha) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha);
    }
}
