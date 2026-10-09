package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.exception.ContaSuspensaException;
import br.com.kauanallyson.kcrm.exception.EmailNaoConfirmadoException;
import br.com.kauanallyson.kcrm.exception.ProblemResponseWriter;
import br.com.kauanallyson.kcrm.exception.Problems;
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

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProblemResponseWriter problemWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AuthenticatedUserService authenticatedUserService,
            ProblemResponseWriter problemWriter
    ) {
        this.jwtService = jwtService;
        this.authenticatedUserService = authenticatedUserService;
        this.problemWriter = problemWriter;
    }

    private static Optional<String> bearerToken(HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return Optional.empty();
        }
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()));
    }

    // O principal é só a identidade (CorretorId ou AdministradorId); credenciais ficam no login
    private static void authenticate(Object principal, Papel papel, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
                .authenticated(principal, null, List.of(papel.authority()));
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
        Optional<Sujeito> sujeito = bearerToken(request).flatMap(jwtService::parse);
        if (sujeito.isPresent()) {
            try {
                // Unresolvable token: continue unauthenticated, the entry point answers 401
                authenticatedUserService.principal(sujeito.get())
                        .ifPresent(principal -> authenticate(principal, sujeito.get().papel(), request));
            } catch (ContaSuspensaException | EmailNaoConfirmadoException e) {
                problemWriter.write(request, response, Problems.of(e.getCode(), e.getMessage()));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
