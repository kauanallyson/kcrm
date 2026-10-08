package br.com.kauanallyson.kcrm.model.historico;

import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

// Um fato registrado no Histórico; o Histórico só cresce, nunca é editado nem apagado
@Entity
@Table(name = "historico", indexes = @Index(name = "idx_historico_registro", columnList = "registro_tipo, registro_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "autor")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private TipoEvento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "registro_tipo", nullable = false, updatable = false, length = 30)
    private TipoRegistro registroTipo;

    @Column(name = "registro_id", nullable = false, updatable = false)
    private UUID registroId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false)
    private Usuario autor;

    @Column(updatable = false)
    private String motivo;

    @Column(updatable = false)
    private String detalhe;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime ocorridoEm;

    public static Evento registrar(TipoEvento tipo, TipoRegistro registroTipo, UUID registroId,
                                   Usuario autor, String motivo, String detalhe) {
        Evento evento = new Evento();
        evento.tipo = Objects.requireNonNull(tipo, "tipo");
        evento.registroTipo = Objects.requireNonNull(registroTipo, "registroTipo");
        evento.registroId = Objects.requireNonNull(registroId, "registroId");
        evento.autor = Objects.requireNonNull(autor, "autor");
        evento.motivo = limpar(motivo);
        evento.detalhe = limpar(detalhe);
        return evento;
    }

    private static String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
