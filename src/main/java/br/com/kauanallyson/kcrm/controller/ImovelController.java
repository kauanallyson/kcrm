package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelRequest;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelResponse;
import br.com.kauanallyson.kcrm.service.Carteira;
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
    private final Carteira carteira;

    public ImovelController(Carteira carteira) {
        this.carteira = carteira;
    }

    @PostMapping
    public ResponseEntity<ImovelResponse> cadastrar(
            @RequestBody @Valid ImovelRequest request,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        ImovelResponse response = ImovelResponse.from(carteira.cadastrarImovel(corretor, request.toDados()));
        return ResponseEntity.created(URI.create("/api/imoveis/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ImovelResponse>> listar(@AuthenticationPrincipal CorretorId corretor) {
        return ResponseEntity.ok(carteira.imoveis(corretor).stream().map(ImovelResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponse> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(ImovelResponse.from(carteira.imovel(corretor, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid ImovelRequest request,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(ImovelResponse.from(carteira.atualizarImovel(corretor, id, request.toDados())));
    }

    // Vendido é definitivo: não há rota de volta a Disponível
    @PostMapping("/{id}/venda")
    public ResponseEntity<ImovelResponse> marcarVendido(
            @PathVariable UUID id,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(ImovelResponse.from(carteira.marcarVendido(corretor, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable UUID id, @AuthenticationPrincipal CorretorId corretor) {
        carteira.apagarImovel(corretor, id);
        return ResponseEntity.noContent().build();
    }
}
