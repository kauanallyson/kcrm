package br.com.kauanallyson.kcrm.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitBucketTest {
    @Test
    void rotasPublicasDeAutenticacaoFicamNoLimiteRigido() {
        assertThat(RateLimitBucket.of(new MockHttpServletRequest("POST", "/api/auth/login")))
                .isEqualTo(RateLimitBucket.AUTH);
        assertThat(RateLimitBucket.of(new MockHttpServletRequest("GET", "/api/clientes")))
                .isEqualTo(RateLimitBucket.API);
    }
}
