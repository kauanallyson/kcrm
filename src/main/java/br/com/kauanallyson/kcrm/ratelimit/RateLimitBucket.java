package br.com.kauanallyson.kcrm.ratelimit;

import br.com.kauanallyson.kcrm.auth.AuthPaths;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public enum RateLimitBucket {
    // Login e cadastro: limite rígido contra força bruta
    AUTH,
    // Todo o resto de /api/**
    API;

    public static RateLimitBucket of(HttpServletRequest request) {
        return AuthPaths.PUBLIC.contains(request.getRequestURI()) ? AUTH : API;
    }

    public String tag() {
        return name().toLowerCase(Locale.ROOT);
    }
}
