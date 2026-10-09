package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthenticatedUserService authenticatedUserService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.jwtService = jwtService;
        this.authenticatedUserService = authenticatedUserService;
    }

    private static Optional<String> bearerToken(HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return Optional.empty();
        }
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()));
    }

    // O principal é só a identidade (CorretorId); credenciais ficam no login
    private static void authenticate(CorretorId principal, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
                .authenticated(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<UUID> id = bearerToken(request).flatMap(jwtService::parse);
        if (id.isPresent()) {
            // Unresolvable token: continue unauthenticated, the entry point answers 401
            authenticatedUserService.principal(id.get())
                    .ifPresent(principal -> authenticate(principal, request));
        }

        filterChain.doFilter(request, response);
    }
}
