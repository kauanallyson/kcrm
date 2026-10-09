package br.com.kauanallyson.kcrm.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitBucketTest {
    @Test
    void encerramentoDeContaConfereSenhaEFicaNoLimiteRigido() {
        assertThat(RateLimitBucket.of(new MockHttpServletRequest("POST", "/api/conta/encerramento")))
                .isEqualTo(RateLimitBucket.AUTH);
        assertThat(RateLimitBucket.of(new MockHttpServletRequest("GET", "/api/conta/exportacao")))
                .isEqualTo(RateLimitBucket.API);
    }
}
