package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteRequest;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteResponse;
import br.com.kauanallyson.kcrm.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser corretor) {
        ClienteResponse response = ClienteResponse.from(clienteService.cadastrar(request, corretor));
        return ResponseEntity.created(URI.create("/api/clientes/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(clienteService.listar(corretor).stream().map(ClienteResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id,
                                                       @AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(ClienteResponse.from(clienteService.buscarPorId(id, corretor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
                                                     @RequestBody @Valid ClienteRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(ClienteResponse.from(clienteService.atualizar(id, request, corretor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser corretor) {
        clienteService.apagar(id, corretor);
        return ResponseEntity.noContent().build();
    }
}
