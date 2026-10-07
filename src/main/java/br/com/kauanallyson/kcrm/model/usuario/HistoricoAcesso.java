package br.com.kauanallyson.kcrm.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

// Uma Desativação ou Reativação de um Usuário; o histórico só cresce, nunca é editado
@Entity
@Table(name = "historico_acesso")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"usuario", "admin"})
public class HistoricoAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacao tipo;

    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Usuario admin;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime ocorridoEm;

    static HistoricoAcesso registrar(Usuario usuario, TipoMovimentacao tipo, String motivo, Usuario admin) {
        HistoricoAcesso historico = new HistoricoAcesso();
        historico.usuario = usuario;
        historico.tipo = tipo;
        historico.motivo = motivo;
        historico.admin = admin;
        return historico;
    }
}
