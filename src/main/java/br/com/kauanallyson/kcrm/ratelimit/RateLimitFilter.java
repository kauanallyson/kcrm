package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.exception.ProblemResponseWriter;
import br.com.kauanallyson.kcrm.exception.Problems;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Limita requisições por IP numa janela fixa guardada em memória.
// O contador é desta instância: com mais de uma instância, cada uma conta separado.
@Component
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilter extends OncePerRequestFilter {
    // De quanto em quanto tempo as janelas já fechadas saem do mapa, para ele não crescer sem fim
    private static final Duration INTERVALO_LIMPEZA = Duration.ofMinutes(1);

    // Chave: "bucket:ip". Valor: a janela atual daquele IP naquele bucket
    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private volatile long proximaLimpeza;

    private final RateLimitProperties properties;
    private final MeterRegistry meterRegistry;
    private final ProblemResponseWriter problemWriter;
    private final Clock clock;

    @Autowired
    public RateLimitFilter(
            RateLimitProperties properties,
            MeterRegistry meterRegistry,
            ProblemResponseWriter problemWriter
    ) {
        this(properties, meterRegistry, problemWriter, Clock.systemUTC());
    }

    // Os testes passam um relógio controlado para avançar o tempo sem esperar
    RateLimitFilter(
            RateLimitProperties properties,
            MeterRegistry meterRegistry,
            ProblemResponseWriter problemWriter,
            Clock clock
    ) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.problemWriter = problemWriter;
        this.clock = clock;
        this.proximaLimpeza = clock.millis() + INTERVALO_LIMPEZA.toMillis();
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
        long agora = clock.millis();
        limparJanelasFechadas(agora);

        RateLimitBucket bucket = RateLimitBucket.of(request);
        RateLimitProperties.Limit limit = properties.limitFor(bucket);
        String chave = bucket.tag() + ":" + request.getRemoteAddr();

        // compute é atômico por chave: duas requisições do mesmo IP não perdem contagem
        Janela janela = janelas.compute(chave, (k, atual) ->
                atual == null || atual.fechada(agora)
                        ? new Janela(1, agora + limit.period().toMillis())
                        : atual.maisUma());

        if (janela.contagem() <= limit.requests()) {
            filterChain.doFilter(request, response);
            return;
        }

        meterRegistry.counter("kcrm.ratelimit.rejected", "bucket", bucket.tag()).increment();
        long restanteMillis = janela.fimMillis() - agora;
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, (restanteMillis + 999) / 1000)));
        problemWriter.write(request, response, Problems.tooManyRequests());
    }

    private void limparJanelasFechadas(long agora) {
        if (agora < proximaLimpeza) {
            return;
        }
        proximaLimpeza = agora + INTERVALO_LIMPEZA.toMillis();
        janelas.values().removeIf(janela -> janela.fechada(agora));
    }

    // Quantidade de requisições no período (imutável) e quando a janela fecha
    private record Janela(int contagem, long fimMillis) {
        boolean fechada(long agora) {
            return agora >= fimMillis;
        }

        Janela maisUma() {
            return new Janela(contagem + 1, fimMillis);
        }
    }

    // Só para testes: quantas janelas estão guardadas
    int janelasGuardadas() {
        return janelas.size();
    }
}
