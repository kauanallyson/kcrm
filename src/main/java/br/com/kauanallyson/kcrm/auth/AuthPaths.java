package br.com.kauanallyson.kcrm.auth;

import java.util.Set;

// Rotas de autenticação abertas ao público; liberadas na segurança e com rate limit mais rígido
public final class AuthPaths {
    public static final String LOGIN = "/api/auth/login";
    public static final String CADASTRO = "/api/auth/cadastro";
    public static final String CONFIRMACAO = "/api/auth/confirmacao";
    public static final String REENVIO_CONFIRMACAO = "/api/auth/confirmacao/reenvio";
    public static final Set<String> PUBLIC = Set.of(LOGIN, CADASTRO, CONFIRMACAO, REENVIO_CONFIRMACAO);
    // Autenticada, mas confere a senha: um token roubado não pode testar senhas no ritmo do resto da API
    public static final String ENCERRAMENTO_DE_CONTA = "/api/conta/encerramento";

    private AuthPaths() {
    }
}
