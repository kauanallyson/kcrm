package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.auth.LoginRequest;
import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import br.com.kauanallyson.kcrm.dto.usuario.UsuarioResponse;
import br.com.kauanallyson.kcrm.service.AuthService;
import br.com.kauanallyson.kcrm.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorId(principal.id())));
    }
}
