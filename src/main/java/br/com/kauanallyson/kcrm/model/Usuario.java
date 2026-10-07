package br.com.kauanallyson.kcrm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
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

    // O default preenche as linhas já existentes quando o ddl-auto=update adiciona a coluna
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'CORRETOR'")
    private Perfil perfil;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    private OffsetDateTime atualizadoEm;

    public static Usuario cadastrar(Dados dados, String senha, PasswordEncoder encoder) {
        Usuario usuario = new Usuario();
        usuario.atualizarDados(dados);
        usuario.perfil = Perfil.CORRETOR;
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

    public record Dados(String nome, Cpf cpf, Email email, String telefone, String endereco) {
    }
}
