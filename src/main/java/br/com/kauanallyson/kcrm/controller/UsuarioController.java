package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.historico.EventoResponse;
import br.com.kauanallyson.kcrm.dto.usuario.CadastroUsuarioRequest;
import br.com.kauanallyson.kcrm.dto.usuario.DesativacaoRequest;
import br.com.kauanallyson.kcrm.dto.usuario.PerfilRequest;
import br.com.kauanallyson.kcrm.dto.usuario.ReativacaoRequest;
import br.com.kauanallyson.kcrm.dto.usuario.UsuarioRequest;
import br.com.kauanallyson.kcrm.dto.usuario.UsuarioResponse;
import br.com.kauanallyson.kcrm.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private static final String ADMIN_OU_PROPRIO = "hasRole('ADMIN') or #id == principal.id";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid CadastroUsuarioRequest request) {
        UsuarioResponse response = UsuarioResponse.from(usuarioService.cadastrar(request));
        return ResponseEntity.created(URI.create("/api/usuarios/" + response.id())).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar().stream().map(UsuarioResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_OU_PROPRIO)
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_OU_PROPRIO)
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable UUID id, @RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.atualizar(id, request)));
    }

    @PatchMapping("/{id}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> mudarPerfil(@PathVariable UUID id,
                                                       @RequestBody @Valid PerfilRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.mudarPerfil(id, request.perfil(), admin.id())));
    }

    @PostMapping("/{id}/desativacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> desativar(@PathVariable UUID id,
                                                     @RequestBody @Valid DesativacaoRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.desativar(id, request.motivo(), admin.id())));
    }

    @PostMapping("/{id}/reativacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> reativar(@PathVariable UUID id,
                                                    @RequestBody(required = false) @Valid ReativacaoRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser admin) {
        String motivo = request == null ? null : request.motivo();
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.reativar(id, motivo, admin.id())));
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventoResponse>> historico(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.historico(id).stream().map(EventoResponse::from).toList());
    }
}
