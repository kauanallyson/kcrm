package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.model.corretor.CorretorId;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelRequest;
import br.com.kauanallyson.kcrm.dto.imovel.ImovelResponse;
import br.com.kauanallyson.kcrm.service.Carteira;
import br.com.kauanallyson.kcrm.dto.common.PaginaResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    // page, size (padrão 20, máximo 100) e sort; só a Carteira do Corretor autenticado
    @GetMapping
    public ResponseEntity<PaginaResponse<ImovelResponse>> listar(
            @ParameterObject @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CorretorId corretor
    ) {
        return ResponseEntity.ok(PaginaResponse.from(carteira.imoveis(corretor, pageable), ImovelResponse::from));
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
