package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.dto.auth.LoginRequest;
import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import br.com.kauanallyson.kcrm.dto.corretor.CadastroCorretorRequest;
import br.com.kauanallyson.kcrm.dto.corretor.CorretorResponse;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.service.AuthService;
import br.com.kauanallyson.kcrm.service.CorretorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CorretorService corretorService;

    public AuthController(AuthService authService, CorretorService corretorService) {
        this.authService = authService;
        this.corretorService = corretorService;
    }

    // Público: o Corretor cria a própria conta
    @PostMapping("/cadastro")
    public ResponseEntity<CorretorResponse> cadastrar(@RequestBody @Valid CadastroCorretorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CorretorResponse.from(corretorService.cadastrar(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CorretorResponse> me(@AuthenticationPrincipal CorretorId corretor) {
        return ResponseEntity.ok(CorretorResponse.from(corretorService.buscarPorId(corretor.value())));
    }
}
