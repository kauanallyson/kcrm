package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.dto.conta.EncerramentoRequest;
import br.com.kauanallyson.kcrm.dto.conta.ExportacaoDaCarteiraResponse;
import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Só o próprio Corretor; o Administrador não tem como apagar contas
@RestController
@RequestMapping("/api/conta")
public class ContaController {
    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping("/exportacao")
    public ResponseEntity<ExportacaoDaCarteiraResponse> exportar(@AuthenticationPrincipal CorretorId corretor) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("carteira.json").build().toString())
                .body(contaService.exportar(corretor));
    }

    @PostMapping("/encerramento")
    public ResponseEntity<Void> encerrar(
            @RequestBody @Valid EncerramentoRequest request,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        contaService.encerrar(corretor, request.senha());
        return ResponseEntity.noContent().build();
    }
}
