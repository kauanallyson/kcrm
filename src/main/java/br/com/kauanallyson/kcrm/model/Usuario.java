package br.com.kauanallyson.kcrm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.kauanallyson.kcrm.exception.MotivoObrigatorioException;
import br.com.kauanallyson.kcrm.exception.UltimoAdminException;
import br.com.kauanallyson.kcrm.exception.UsuarioJaAtivoException;
import br.com.kauanallyson.kcrm.exception.UsuarioJaDesativadoException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private Cpf cpf;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private Email email;

    @Column(name = "senha", nullable = false)
    private SenhaHash senhaHash;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private boolean ativo;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    private OffsetDateTime atualizadoEm;

    public static Usuario cadastrar(Dados dados, Perfil perfil, String senha, PasswordEncoder encoder) {
        Usuario usuario = new Usuario();
        usuario.atualizarDados(dados);
        usuario.perfil = Objects.requireNonNull(perfil, "perfil");
        usuario.ativo = true;
        usuario.senhaHash = SenhaHash.encode(senha, encoder);
        return usuario;
    }

    public void atualizarDados(Dados dados) {
        this.nome = dados.nome();
        this.cpf = dados.cpf();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.endereco = dados.endereco();
    }

    // Gerar um novo hash para uma senha inalterada faria toda edição de dados parecer uma troca de senha
    public void alterarSenha(String senha, PasswordEncoder encoder) {
        if (senhaConfere(senha, encoder)) {
            return;
        }
        this.senhaHash = SenhaHash.encode(senha, encoder);
    }

    public boolean senhaConfere(String senha, PasswordEncoder encoder) {
        return senhaHash.matches(senha, encoder);
    }

    public boolean isAdminAtivo() {
        return ativo && perfil == Perfil.ADMIN;
    }

    // adminsAtivos é a contagem atual de Admins ativos, incluindo este Usuário se ele for um
    public void mudarPerfil(Perfil novo, long adminsAtivos) {
        Objects.requireNonNull(novo, "perfil");
        if (novo != Perfil.ADMIN) {
            exigirQueNaoSejaOUltimoAdmin(adminsAtivos);
        }
        this.perfil = novo;
    }

    public HistoricoAcesso desativar(String motivo, Usuario admin, long adminsAtivos) {
        exigirMotivo(motivo);
        if (!ativo) {
            throw new UsuarioJaDesativadoException(id);
        }
        exigirQueNaoSejaOUltimoAdmin(adminsAtivos);
        this.ativo = false;
        return HistoricoAcesso.registrar(this, TipoMovimentacao.DESATIVACAO, motivo.strip(), admin);
    }

    public HistoricoAcesso reativar(String motivo, Usuario admin) {
        if (ativo) {
            throw new UsuarioJaAtivoException(id);
        }
        this.ativo = true;
        String motivoLimpo = motivo == null || motivo.isBlank() ? null : motivo.strip();
        return HistoricoAcesso.registrar(this, TipoMovimentacao.REATIVACAO, motivoLimpo, admin);
    }

    private void exigirQueNaoSejaOUltimoAdmin(long adminsAtivos) {
        if (isAdminAtivo() && adminsAtivos <= 1) {
            throw new UltimoAdminException();
        }
    }

    private static void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new MotivoObrigatorioException();
        }
    }

    public record Dados(String nome, Cpf cpf, Email email, String telefone, String endereco) {
    }
}
