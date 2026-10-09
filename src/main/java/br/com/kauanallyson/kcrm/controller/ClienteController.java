package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteRequest;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteResponse;
import br.com.kauanallyson.kcrm.service.Carteira;
import br.com.kauanallyson.kcrm.dto.common.PaginaResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final Carteira carteira;

    public ClienteController(Carteira carteira) {
        this.carteira = carteira;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(
            @RequestBody @Valid ClienteRequest request,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        ClienteResponse response = ClienteResponse.from(carteira.cadastrarCliente(corretor, request.toDados()));
        return ResponseEntity.created(URI.create("/api/clientes/" + response.id())).body(response);
    }

    // page, size (padrão 20, máximo 100) e sort; só a Carteira do Corretor autenticado
    @GetMapping
    public ResponseEntity<PaginaResponse<ClienteResponse>> listar(
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(PaginaResponse.from(carteira.clientes(corretor, pageable), ClienteResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(ClienteResponse.from(carteira.cliente(corretor, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid ClienteRequest request,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(ClienteResponse.from(carteira.atualizarCliente(corretor, id, request.toDados())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable UUID id, @AuthenticationPrincipal CorretorId corretor) {
        carteira.apagarCliente(corretor, id);
        return ResponseEntity.noContent().build();
    }
}
