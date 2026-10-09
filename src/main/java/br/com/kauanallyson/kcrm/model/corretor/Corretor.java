package br.com.kauanallyson.kcrm.model.corretor;

import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

// Quem usa o CRM; cria a própria conta e só enxerga os seus Clientes e Imóveis
@Entity
@Table(name = "corretores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Corretor {
    private static final int CRECI_MAXIMO = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private Email email;

    @Column(name = "senha", nullable = false)
    private SenhaHash senhaHash;

    @Column(nullable = false, length = CRECI_MAXIMO)
    private String creci;

    @Column(nullable = false, length = 15)
    private Telefone whatsapp;

    // Suspensão pelo Administrador: bloqueia o acesso, mas a Carteira fica intacta
    @Column(nullable = false)
    private boolean suspenso;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    private OffsetDateTime atualizadoEm;

    public static Corretor cadastrar(
            Dados dados,
            String senha,
            PasswordEncoder encoder
    ) {
        if (dados.nome() == null || dados.nome().isBlank()) {
            throw new ValorInvalidoException("nome", "O nome não pode ficar em branco");
        }
        if (dados.email() == null) {
            throw new ValorInvalidoException("email", "O e-mail não pode ficar em branco");
        }
        if (dados.whatsapp() == null) {
            throw new ValorInvalidoException("whatsapp", "O WhatsApp não pode ficar em branco");
        }
        if (dados.creci() == null || dados.creci().isBlank()) {
            throw new ValorInvalidoException("creci", "O CRECI não pode ficar em branco");
        }
        if (dados.creci().strip().length() > CRECI_MAXIMO) {
            throw new ValorInvalidoException("creci", "O CRECI deve ter no máximo " + CRECI_MAXIMO + " caracteres");
        }
        Corretor corretor = new Corretor();
        corretor.nome = dados.nome().strip();
        corretor.email = dados.email();
        corretor.creci = dados.creci().strip().toUpperCase();
        corretor.whatsapp = dados.whatsapp();
        corretor.senhaHash = SenhaHash.encode(senha, encoder);
        return corretor;
    }

    // Idempotentes: suspender um Suspenso ou reativar um ativo não muda nada
    public void suspender() {
        suspenso = true;
    }

    public void reativar() {
        suspenso = false;
    }

    public record Dados(
            String nome,
            Email email,
            String creci,
            Telefone whatsapp
    ) {
    }
}
