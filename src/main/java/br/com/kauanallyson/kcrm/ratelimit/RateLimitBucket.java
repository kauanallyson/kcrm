package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.auth.AuthPaths;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public enum RateLimitBucket {
    // Login, cadastro e o que confere senha: limite rígido contra força bruta
    AUTH,
    // Todo o resto de /api/**
    API;

    public static RateLimitBucket of(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return AuthPaths.PUBLIC.contains(uri) || AuthPaths.ENCERRAMENTO_DE_CONTA.equals(uri) ? AUTH : API;
    }

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }
}
