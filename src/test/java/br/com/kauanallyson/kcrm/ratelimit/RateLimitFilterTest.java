package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.exception.ProblemResponseWriter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {
    private SimpleMeterRegistry meterRegistry;
    private RelogioDeTeste relogio;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        relogio = new RelogioDeTeste();
        RateLimitProperties properties = new RateLimitProperties(
                new RateLimitProperties.Limit(2, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(3, Duration.ofMinutes(1)));
        ProblemResponseWriter problemWriter = new ProblemResponseWriter(JsonMapper.builder().build());
        filter = new RateLimitFilter(properties, meterRegistry, problemWriter, relogio);
    }

    @Test
    void bloqueiaLoginAcimaDoLimiteCom429() throws Exception {
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);

        relogio.avancar(Duration.ofSeconds(20));
        MockHttpServletResponse bloqueada = call("/api/auth/login", "10.0.0.1");

        assertThat(bloqueada.getStatus()).isEqualTo(429);
        assertThat(bloqueada.getHeader("Retry-After")).isEqualTo("40");
        assertThat(bloqueada.getContentAsString()).contains("MUITAS_REQUISICOES");
        assertThat(meterRegistry.counter("kcrm.ratelimit.rejected", "bucket", "auth").count()).isEqualTo(1);
    }

    @Test
    void liberaDeNovoQuandoAJanelaFecha() throws Exception {
        call("/api/auth/login", "10.0.0.1");
        call("/api/auth/login", "10.0.0.1");
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(429);

        relogio.avancar(Duration.ofMinutes(1));

        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(429);
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
    void esqueceJanelasFechadasParaOMapaNaoCrescerSemFim() throws Exception {
        call("/api/clientes", "10.0.0.1");
        call("/api/clientes", "10.0.0.2");
        assertThat(filter.janelasGuardadas()).isEqualTo(2);

        relogio.avancar(Duration.ofMinutes(2));
        call("/api/clientes", "10.0.0.3");

        assertThat(filter.janelasGuardadas()).isEqualTo(1);
    }

    private MockHttpServletResponse call(String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    // Relógio parado que só anda quando o teste manda
    private static class RelogioDeTeste extends Clock {
        private Instant agora = Instant.parse("2026-01-01T00:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public Instant instant() {
            return agora;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
