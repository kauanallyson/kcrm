package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteRequest;
import br.com.kauanallyson.kcrm.dto.cliente.ClienteResponse;
import br.com.kauanallyson.kcrm.dto.cliente.TransferenciaRequest;
import br.com.kauanallyson.kcrm.dto.historico.EventoResponse;
import br.com.kauanallyson.kcrm.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

// Clientes nunca são apagados: não há DELETE
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody @Valid ClienteRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser usuario) {
        ClienteResponse response = ClienteResponse.from(clienteService.cadastrar(request, usuario));
        return ResponseEntity.created(URI.create("/api/clientes/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@AuthenticationPrincipal AuthenticatedUser usuario) {
        return ResponseEntity.ok(clienteService.listar(usuario).stream().map(ClienteResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id,
                                                       @AuthenticationPrincipal AuthenticatedUser usuario) {
        return ResponseEntity.ok(ClienteResponse.from(clienteService.buscarPorId(id, usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable UUID id,
                                                     @RequestBody @Valid ClienteRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser usuario) {
        return ResponseEntity.ok(ClienteResponse.from(clienteService.atualizar(id, request, usuario)));
    }

    @PostMapping("/{id}/transferencia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> transferir(@PathVariable UUID id,
                                                      @RequestBody @Valid TransferenciaRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ClienteResponse.from(
                clienteService.transferir(id, request.corretorId(), request.motivo(), admin)));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<EventoResponse>> historico(@PathVariable UUID id,
                                                          @AuthenticationPrincipal AuthenticatedUser usuario) {
        return ResponseEntity.ok(clienteService.historico(id, usuario).stream().map(EventoResponse::from).toList());
    }
}
