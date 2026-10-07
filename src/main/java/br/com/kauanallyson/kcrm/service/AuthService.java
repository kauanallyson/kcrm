package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.JwtService;
import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.LoginRequest;
import br.com.kauanallyson.kcrm.dto.TokenResponse;
import br.com.kauanallyson.kcrm.dto.UsuarioRequest;
import br.com.kauanallyson.kcrm.exception.CredenciaisInvalidasException;
import br.com.kauanallyson.kcrm.model.Usuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    public TokenResponse login(LoginRequest request) {
        return jwtService.issue(authenticate(request).id());
    }

    public TokenResponse cadastrar(UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrar(request);
        return jwtService.issue(usuario.getId());
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
