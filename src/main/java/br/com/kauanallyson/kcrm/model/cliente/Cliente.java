package br.com.kauanallyson.kcrm.model.cliente;

import br.com.kauanallyson.kcrm.exception.CorretorInvalidoException;
import br.com.kauanallyson.kcrm.exception.TransferenciaParaOMesmoCorretorException;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Pessoa atendida pela imobiliária; nunca é apagada. CPF, e-mail e endereço são coletados ao longo do Atendimento
@Entity
@Table(name = "clientes", indexes = @Index(name = "idx_clientes_corretor", columnList = "corretor_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "corretor")
public class Cliente {
    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "dos", "das", "e");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    // Não é único: dois Clientes podem ter o mesmo WhatsApp
    @Column(nullable = false, length = 15)
    private Telefone whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Origem origem;

    @Column(name = "indicado_por")
    private String indicadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corretor_id", nullable = false)
    private Usuario corretor;

    @Column(length = 14)
    private Cpf cpf;

    private Email email;

    // Endereco declara suas colunas como obrigatórias; no Cliente ele inteiro é opcional
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rua", column = @Column(name = "endereco_rua", length = 150)),
            @AttributeOverride(name = "numero", column = @Column(name = "endereco_numero", length = 10)),
            @AttributeOverride(name = "complemento", column = @Column(name = "endereco_complemento", length = 100)),
            @AttributeOverride(name = "bairro", column = @Column(name = "endereco_bairro", length = 100)),
            @AttributeOverride(name = "cidade", column = @Column(name = "endereco_cidade", length = 100)),
            @AttributeOverride(name = "estado", column = @Column(name = "endereco_estado", length = 2)),
            @AttributeOverride(name = "cep", column = @Column(name = "endereco_cep", length = 9))
    })
    private Endereco endereco;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    private OffsetDateTime atualizadoEm;

    public static Cliente cadastrar(Dados dados, Usuario corretor) {
        exigirCorretorAtivo(corretor);
        Cliente cliente = new Cliente();
        cliente.atualizarDados(dados);
        cliente.corretor = corretor;
        return cliente;
    }

    public void atualizarDados(Dados dados) {
        if (dados.nome() == null || dados.nome().isBlank()) {
            throw new ValorInvalidoException("nome", "O nome não pode ficar em branco");
        }
        if (dados.whatsapp() == null) {
            throw new ValorInvalidoException("whatsapp", "O WhatsApp não pode ficar em branco");
        }
        if (dados.origem() == null) {
            throw new ValorInvalidoException("origem", "A origem não pode ficar em branco");
        }
        this.nome = dados.nome().strip();
        this.whatsapp = dados.whatsapp();
        this.origem = dados.origem();
        this.indicadoPor = indicadoPorDa(dados.origem(), dados.indicadoPor());
        this.cpf = dados.cpf();
        this.email = dados.email();
        this.endereco = dados.endereco();
    }

    // Retorna o Corretor anterior, para a Transferência entrar no Histórico
    public Usuario transferir(Usuario novo) {
        exigirCorretorAtivo(novo);
        if (novo == corretor || (novo.getId() != null && novo.getId().equals(corretor.getId()))) {
            throw new TransferenciaParaOMesmoCorretorException();
        }
        Usuario anterior = corretor;
        this.corretor = novo;
        return anterior;
    }

    private static void exigirCorretorAtivo(Usuario usuario) {
        if (usuario == null || !usuario.isAtivo() || usuario.getPerfil() != Perfil.CORRETOR) {
            throw new CorretorInvalidoException();
        }
    }

    // Obrigatório só na Indicação; normalizado como nome próprio: "joão DA silva" vira "João da Silva"
    private static String indicadoPorDa(Origem origem, String indicadoPor) {
        boolean informado = indicadoPor != null && !indicadoPor.isBlank();
        if (origem != Origem.INDICACAO) {
            if (informado) {
                throw new ValorInvalidoException("indicadoPor", "Só informe quem indicou quando a origem for Indicação");
            }
            return null;
        }
        if (!informado) {
            throw new ValorInvalidoException("indicadoPor", "Informe o nome de quem indicou o Cliente");
        }
        if (indicadoPor.strip().length() > 255) {
            throw new ValorInvalidoException("indicadoPor", "O nome de quem indicou deve ter no máximo 255 caracteres");
        }
        String[] palavras = indicadoPor.strip().toLowerCase().split("\\s+");
        List<String> normalizadas = new ArrayList<>();
        for (int i = 0; i < palavras.length; i++) {
            String palavra = palavras[i];
            if (i > 0 && PARTICULAS.contains(palavra)) {
                normalizadas.add(palavra);
            } else {
                normalizadas.add(palavra.substring(0, 1).toUpperCase() + palavra.substring(1));
            }
        }
        return String.join(" ", normalizadas);
    }

    public record Dados(String nome, Telefone whatsapp, Origem origem, String indicadoPor,
                        Cpf cpf, Email email, Endereco endereco) {
    }
}
