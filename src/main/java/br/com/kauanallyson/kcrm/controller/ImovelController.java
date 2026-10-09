package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelRequest;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelResponse;
import br.com.kauanallyson.kcrm.service.ImovelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/imoveis")
public class ImovelController {
    private final ImovelService imovelService;

    public ImovelController(ImovelService imovelService) {
        this.imovelService = imovelService;
    }

    @PostMapping
    public ResponseEntity<ImovelResponse> cadastrar(@RequestBody @Valid ImovelRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser corretor) {
        ImovelResponse response = ImovelResponse.from(imovelService.cadastrar(request, corretor));
        return ResponseEntity.created(URI.create("/api/imoveis/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ImovelResponse>> listar(@AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(imovelService.listar(corretor).stream().map(ImovelResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponse> buscarPorId(@PathVariable UUID id,
                                                      @AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(ImovelResponse.from(imovelService.buscarPorId(id, corretor)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponse> atualizar(@PathVariable UUID id,
                                                    @RequestBody @Valid ImovelRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(ImovelResponse.from(imovelService.atualizar(id, request, corretor)));
    }

    // Vendido é definitivo: não há rota de volta a Disponível
    @PostMapping("/{id}/venda")
    public ResponseEntity<ImovelResponse> marcarVendido(@PathVariable UUID id,
                                                        @AuthenticationPrincipal AuthenticatedUser corretor) {
        return ResponseEntity.ok(ImovelResponse.from(imovelService.marcarVendido(id, corretor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser corretor) {
        imovelService.apagar(id, corretor);
        return ResponseEntity.noContent().build();
    }
}
