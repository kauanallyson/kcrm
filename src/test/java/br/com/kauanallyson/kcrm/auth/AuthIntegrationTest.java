package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestcontainersConfig;
import br.com.kauanallyson.kcrm.repository.UserRepository;
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
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerReturnsTokenAndHashesPassword() throws Exception {
        String email = randomEmail();

        postJson("/api/auth/register", userJson(email, randomCpf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));

        String stored = userRepository.findByEmail(email).orElseThrow().getPassword();
        assertThat(stored).startsWith("{bcrypt}").doesNotContain(PASSWORD);
    }

    @Test
    void registerWithDuplicateEmailIsConflict() throws Exception {
        String email = randomEmail();
        register(email);

        postJson("/api/auth/register", userJson(email, randomCpf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void registerWithDuplicateCpfIsConflict() throws Exception {
        String cpf = randomCpf();
        register(randomEmail(), cpf);

        postJson("/api/auth/register", userJson(randomEmail(), cpf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        String email = randomEmail();
        register(email);

        postJson("/api/auth/login", loginJson(email, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordOrUnknownEmailIsUnauthorizedWithSameMessage() throws Exception {
        String email = randomEmail();
        register(email);

        postJson("/api/auth/login", loginJson(email, "wrongpass123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        postJson("/api/auth/login", loginJson(randomEmail(), PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void meReturnsTheLoggedInUser() throws Exception {
        String email = randomEmail();
        String token = register(email);

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idOf(email).toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void meWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void protectedRouteWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void protectedRouteWithInvalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/" + UUID.randomUUID()).header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanAccessOnlyThemselves() throws Exception {
        String emailA = randomEmail();
        String cpfA = randomCpf();
        String tokenA = register(emailA, cpfA);
        String emailB = randomEmail();
        register(emailB);
        UUID idA = idOf(emailA);
        UUID idB = idOf(emailB);

        mockMvc.perform(get("/api/users/" + idA).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());
        mockMvc.perform(json(put("/api/users/" + idA), userJson(emailA, cpfA)).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + idB).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(put("/api/users/" + idB), userJson(emailB, randomCpf())).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/" + idB).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void userCanDeleteThemselvesAndTheirTokenStopsWorking() throws Exception {
        String email = randomEmail();
        String token = register(email);
        UUID id = idOf(email);

        mockMvc.perform(delete("/api/users/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAndCreateUserRoutesAreGone() throws Exception {
        String token = register(randomEmail());

        mockMvc.perform(get("/api/users").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(post("/api/users"), userJson(randomEmail(), randomCpf())).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void malformedJsonIsBadRequest() throws Exception {
        postJson("/api/auth/login", "{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void invalidFieldsReturnValidationErrors() throws Exception {
        postJson("/api/auth/register", userJson("not-an-email", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.cpf").exists());
    }

    @Test
    void invalidIdIsBadRequest() throws Exception {
        String token = register(randomEmail());

        mockMvc.perform(get("/api/users/not-a-uuid").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private String register(String email) throws Exception {
        return register(email, randomCpf());
    }

    private String register(String email, String cpf) throws Exception {
        String body = postJson("/api/auth/register", userJson(email, cpf))
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
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String randomEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    // Builds a CPF with valid check digits so it passes @CPF validation
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

    private static String userJson(String email, String cpf) {
        return """
                {"name":"Test","cpf":"%s","email":"%s","password":"%s","phone":"1","address":"a"}
                """.formatted(cpf, email, PASSWORD);
    }

    private static String loginJson(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }
}
