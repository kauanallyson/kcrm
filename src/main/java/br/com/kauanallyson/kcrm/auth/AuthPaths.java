package br.com.kauanallyson.kcrm.auth;

import java.util.Set;

// Rotas de autenticação abertas ao público; liberadas na segurança e com rate limit mais rígido
public final class AuthPaths {
    public static final String LOGIN = "/api/auth/login";
    public static final String CADASTRO = "/api/auth/cadastro";
    public static final Set<String> PUBLIC = Set.of(LOGIN, CADASTRO);

    private AuthPaths() {
    }
}
