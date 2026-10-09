package br.com.kauanallyson.kcrm.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

// O Spring passaria "${DB_PASSWORD}" literal ao banco se a variável faltasse; aqui o startup cai antes,
// dizendo quais faltam. Em dev elas vêm do application-dev.properties, em prod do ambiente
public class SegredosObrigatorios implements EnvironmentPostProcessor, Ordered {
    private static final List<String> SEGREDOS = List.of("DB_USERNAME", "DB_PASSWORD",
            "SMTP_HOST", "SMTP_USERNAME", "SMTP_PASSWORD", "EMAIL_REMETENTE", "CONFIRMACAO_URL_BASE");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> faltando = SEGREDOS.stream()
                .filter(segredo -> !environment.containsProperty(segredo))
                .toList();
        if (!faltando.isEmpty()) {
            throw new IllegalStateException("Variáveis de ambiente obrigatórias não definidas: " + String.join(", ", faltando));
        }
    }

    // Depois do ConfigData, para enxergar os application-*.properties e o .env
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
