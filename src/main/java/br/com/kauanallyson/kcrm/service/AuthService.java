package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auditoria.Auditoria;
import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.auth.JwtService;
import br.com.kauanallyson.kcrm.auth.Papel;
import br.com.kauanallyson.kcrm.dto.auth.LoginRequest;
import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import br.com.kauanallyson.kcrm.exception.ContaSuspensaException;
import br.com.kauanallyson.kcrm.exception.CredenciaisInvalidasException;
import br.com.kauanallyson.kcrm.exception.EmailNaoConfirmadoException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Auditoria auditoria;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, Auditoria auditoria) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditoria = auditoria;
    }

    public TokenResponse login(LoginRequest request) {
        AuthenticatedUser usuario = authenticate(request);
        // Só depois da senha certa: a quem não a sabe, nada disso é revelado
        if (!usuario.emailConfirmado()) {
            throw new EmailNaoConfirmadoException();
        }
        if (usuario.suspenso()) {
            auditoria.registrar("login.suspenso", usuario.id(), null);
            throw new ContaSuspensaException();
        }
        if (usuario.papel() == Papel.ADMINISTRADOR) {
            auditoria.registrar("administrador.login", null, usuario.id());
        } else {
            auditoria.registrar("login", usuario.id(), null);
        }
        return jwtService.issue(usuario.id(), usuario.papel());
    }

    private AuthenticatedUser authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
            return (AuthenticatedUser) authentication.getPrincipal();
        } catch (AuthenticationException e) {
            // Sem e-mail no log: a tentativa fica rastreável pelo id da requisição
            auditoria.registrar("login.falha", null, null);
            throw new CredenciaisInvalidasException();
        }
    }
}
