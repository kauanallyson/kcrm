package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.dto.auth.ConfirmacaoRequest;
import br.com.kauanallyson.kcrm.dto.auth.LoginRequest;
import br.com.kauanallyson.kcrm.dto.auth.ReenvioConfirmacaoRequest;
import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import br.com.kauanallyson.kcrm.dto.corretor.CadastroCorretorRequest;
import br.com.kauanallyson.kcrm.dto.corretor.CorretorResponse;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.service.AuthService;
import br.com.kauanallyson.kcrm.service.ConfirmacaoDeEmailService;
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
    private final ConfirmacaoDeEmailService confirmacaoDeEmail;

    public AuthController(
            AuthService authService,
            CorretorService corretorService,
            ConfirmacaoDeEmailService confirmacaoDeEmail
    ) {
        this.authService = authService;
        this.corretorService = corretorService;
        this.confirmacaoDeEmail = confirmacaoDeEmail;
    }

    // Público: o Corretor cria a própria conta, que só fica utilizável após a Confirmação de E-mail.
    // Se o e-mail é de uma conta ainda não confirmada, o link é reenviado: 202, sem expor os dados dela
    @PostMapping("/cadastro")
    public ResponseEntity<CorretorResponse> cadastrar(@RequestBody @Valid CadastroCorretorRequest request) {
        return corretorService.cadastrar(request)
                .map(corretor -> ResponseEntity.status(HttpStatus.CREATED).body(CorretorResponse.from(corretor)))
                .orElseGet(() -> ResponseEntity.accepted().build());
    }

    @PostMapping("/confirmacao")
    public ResponseEntity<Void> confirmar(@RequestBody @Valid ConfirmacaoRequest request) {
        confirmacaoDeEmail.confirmar(request.token());
        return ResponseEntity.noContent().build();
    }

    // Sempre 202: a resposta não revela se o e-mail existe nem se já foi confirmado
    @PostMapping("/confirmacao/reenvio")
    public ResponseEntity<Void> reenviar(@RequestBody @Valid ReenvioConfirmacaoRequest request) {
        confirmacaoDeEmail.reenviar(request.email());
        return ResponseEntity.accepted().build();
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
