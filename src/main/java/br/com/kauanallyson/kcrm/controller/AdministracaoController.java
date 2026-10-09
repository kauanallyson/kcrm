package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.dto.administracao.CorretorAdministradoResponse;
import br.com.kauanallyson.kcrm.service.Administracao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Só o Administrador chega aqui (SecurityConfig)
@RestController
@RequestMapping("/api/administracao/corretores")
public class AdministracaoController {
    private final Administracao administracao;

    public AdministracaoController(Administracao administracao) {
        this.administracao = administracao;
    }

    @GetMapping
    public ResponseEntity<List<CorretorAdministradoResponse>> listar() {
        return ResponseEntity.ok(administracao.corretores().stream().map(CorretorAdministradoResponse::from).toList());
    }

    // PUT e DELETE na Suspensão: os dois são idempotentes
    @PutMapping("/{id}/suspensao")
    public ResponseEntity<Void> suspender(@PathVariable UUID id) {
        administracao.suspender(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/suspensao")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        administracao.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
