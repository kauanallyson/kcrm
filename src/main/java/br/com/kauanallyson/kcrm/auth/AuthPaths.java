package br.com.kauanallyson.kcrm.auth;

import java.util.Set;

// Rotas de autenticação abertas ao público; liberadas na segurança e com rate limit mais rígido
public final class AuthPaths {
    public static final String LOGIN = "/api/auth/login";
    public static final String CADASTRO = "/api/auth/cadastro";
    public static final String CONFIRMACAO = "/api/auth/confirmacao";
    public static final String REENVIO_CONFIRMACAO = "/api/auth/confirmacao/reenvio";
    public static final Set<String> PUBLIC = Set.of(LOGIN, CADASTRO, CONFIRMACAO, REENVIO_CONFIRMACAO);

    private AuthPaths() {
    }
}
