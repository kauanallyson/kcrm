package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.exception.ProblemResponseWriter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redis;

    private SimpleMeterRegistry meterRegistry;
    private RateLimitFilter filter;

    @BeforeAll
    static void startRedis() {
        REDIS.start();
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redis = new StringRedisTemplate(connectionFactory);
    }

    @AfterAll
    static void stopRedis() {
        connectionFactory.destroy();
        REDIS.stop();
    }

    @BeforeEach
    void setUp() {
        redis.execute(connection -> {
            connection.serverCommands().flushAll();
            return null;
        }, true);
        meterRegistry = new SimpleMeterRegistry();
        filter = filter(redis, 2, 3);
    }

    @Test
    void bloqueiaLoginAcimaDoLimiteCom429() throws Exception {
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);

        MockHttpServletResponse bloqueada = call("/api/auth/login", "10.0.0.1");

        assertThat(bloqueada.getStatus()).isEqualTo(429);
        assertThat(Long.parseLong(bloqueada.getHeader("Retry-After"))).isBetween(1L, 60L);
        assertThat(bloqueada.getContentAsString()).contains("MUITAS_REQUISICOES");
        assertThat(meterRegistry.counter("kcrm.ratelimit.rejected", "bucket", "auth").count()).isEqualTo(1);
    }

    @Test
    void contaCadaIpSeparadamente() throws Exception {
        call("/api/auth/login", "10.0.0.1");
        call("/api/auth/login", "10.0.0.1");

        assertThat(call("/api/auth/login", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void loginNaoConsomeOLimiteDaApi() throws Exception {
        call("/api/auth/login", "10.0.0.1");
        call("/api/auth/login", "10.0.0.1");
        call("/api/auth/login", "10.0.0.1");

        assertThat(call("/api/clientes", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void ignoraRotasForaDaApi() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(call("/actuator/prometheus", "10.0.0.1").getStatus()).isEqualTo(200);
        }
    }

    @Test
    void liberaQuandoORedisCai() throws Exception {
        LettuceConnectionFactory semRedis = new LettuceConnectionFactory("localhost", 1);
        semRedis.afterPropertiesSet();
        semRedis.start();
        try {
            RateLimitFilter semLimite = filter(new StringRedisTemplate(semRedis), 1, 1);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/clientes");
            MockHttpServletResponse response = new MockHttpServletResponse();
            semLimite.doFilter(request, response, new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(meterRegistry.counter("kcrm.ratelimit.failures").count()).isEqualTo(1);
        } finally {
            semRedis.destroy();
        }
    }

    private RateLimitFilter filter(StringRedisTemplate redis, int authRequests, int apiRequests) {
        RateLimitProperties properties = new RateLimitProperties(
                new RateLimitProperties.Limit(authRequests, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(apiRequests, Duration.ofMinutes(1)));
        ProblemResponseWriter problemWriter = new ProblemResponseWriter(JsonMapper.builder().build());
        return new RateLimitFilter(redis, properties, meterRegistry, problemWriter);
    }

    private MockHttpServletResponse call(String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
