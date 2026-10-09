package br.com.kauanallyson.kcrm.model.administrador;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.SenhaHash;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

// Quem opera a plataforma; existe um único. Não é Corretor e não tem Carteira
@Entity
@Table(name = "administradores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Administrador {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private Email email;

    @Column(name = "senha", nullable = false)
    private SenhaHash senhaHash;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    public static Administrador criar(Email email, String senha, PasswordEncoder encoder) {
        Administrador administrador = new Administrador();
        administrador.email = email;
        administrador.senhaHash = SenhaHash.encode(senha, encoder);
        return administrador;
    }
}
