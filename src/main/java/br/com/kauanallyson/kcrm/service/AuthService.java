package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.auth.JwtService;
import br.com.kauanallyson.kcrm.dto.auth.LoginRequest;
import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import br.com.kauanallyson.kcrm.exception.ContaSuspensaException;
import br.com.kauanallyson.kcrm.exception.CredenciaisInvalidasException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        AuthenticatedUser usuario = authenticate(request);
        // Só depois da senha certa: a quem não a sabe, a Suspensão não é revelada
        if (usuario.suspenso()) {
            throw new ContaSuspensaException();
        }
        return jwtService.issue(usuario.id(), usuario.papel());
    }

    private AuthenticatedUser authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
            return (AuthenticatedUser) authentication.getPrincipal();
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException();
        }
    }
}
