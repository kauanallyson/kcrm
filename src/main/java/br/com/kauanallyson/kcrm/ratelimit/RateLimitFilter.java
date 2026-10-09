package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.exception.ProblemResponseWriter;
import br.com.kauanallyson.kcrm.exception.Problems;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Limita requisições por IP numa janela fixa contada no Redis, compartilhada entre as instâncias
@Component
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Ver scripts/increment_window.lua: devolve {contagem, ms até a janela fechar}
    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> INCREMENT_WINDOW =
            RedisScript.of(new ClassPathResource("scripts/increment_window.lua"), List.class);

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;
    private final MeterRegistry meterRegistry;
    private final ProblemResponseWriter problemWriter;

    public RateLimitFilter(
            StringRedisTemplate redis,
            RateLimitProperties properties,
            MeterRegistry meterRegistry,
            ProblemResponseWriter problemWriter
    ) {
        this.redis = redis;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.problemWriter = problemWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        RateLimitBucket bucket = RateLimitBucket.of(request);
        RateLimitProperties.Limit limit = properties.limitFor(bucket);

        List<?> window;
        try {
            window = redis.execute(INCREMENT_WINDOW,
                    List.of("ratelimit:" + bucket.tag() + ":" + request.getRemoteAddr()),
                    String.valueOf(limit.period().toMillis()));
        } catch (RuntimeException ex) {
            // Redis fora não derruba a API: segue sem limite e registra
            log.warn("Rate limit indisponível, liberando a requisição", ex);
            meterRegistry.counter("kcrm.ratelimit.failures").increment();
            filterChain.doFilter(request, response);
            return;
        }

        long count = (Long) window.getFirst();
        if (count <= limit.requests()) {
            filterChain.doFilter(request, response);
            return;
        }

        meterRegistry.counter("kcrm.ratelimit.rejected", "bucket", bucket.tag()).increment();
        long remainingMillis = (Long) window.get(1);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, (remainingMillis + 999) / 1000)));
        problemWriter.write(request, response, Problems.tooManyRequests());
    }
}
