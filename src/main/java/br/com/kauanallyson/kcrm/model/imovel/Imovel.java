package br.com.kauanallyson.kcrm.model.imovel;

import br.com.kauanallyson.kcrm.exception.ImovelVendidoException;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Bem que o Corretor oferece à venda. As Características são todas opcionais e valem para qualquer Tipo
@Entity
@Table(name = "imoveis", indexes = @Index(name = "idx_imoveis_corretor", columnList = "corretor_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "corretor")
public class Imovel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corretor_id", nullable = false)
    private Corretor corretor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Embedded
    private Endereco endereco;

    @Column(name = "preco_venda", nullable = false, precision = 14, scale = 2)
    private BigDecimal precoVenda;

    @Embedded
    private Proprietario proprietario;

    // A área é informada à parte: terrenos irregulares não seguem frente × fundo
    @Column(precision = 10, scale = 2)
    private BigDecimal area;

    @Column(precision = 10, scale = 2)
    private BigDecimal frente;

    @Column(precision = 10, scale = 2)
    private BigDecimal fundo;

    private Integer quartos;

    private Integer suites;

    private Integer banheiros;

    private Integer vagas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Situacao situacao;

    @CreationTimestamp
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    private OffsetDateTime atualizadoEm;

    public static Imovel cadastrar(Dados dados, Corretor corretor) {
        Imovel imovel = new Imovel();
        imovel.corretor = corretor;
        imovel.situacao = Situacao.DISPONIVEL;
        imovel.atualizarDados(dados);
        return imovel;
    }

    public void atualizarDados(Dados dados) {
        exigirDisponivel();
        if (dados.tipo() == null) {
            throw new ValorInvalidoException("tipo", "O tipo não pode ficar em branco");
        }
        if (dados.endereco() == null) {
            throw new ValorInvalidoException("endereco", "O endereço não pode ficar em branco");
        }
        if (dados.proprietario() == null) {
            throw new ValorInvalidoException("proprietario", "O Proprietário não pode ficar em branco");
        }
        if (dados.precoVenda() == null) {
            throw new ValorInvalidoException("precoVenda", "O preço de venda não pode ficar em branco");
        }
        if (dados.precoVenda().signum() <= 0) {
            throw new ValorInvalidoException("precoVenda", "O preço de venda deve ser maior que zero");
        }
        this.tipo = dados.tipo();
        this.endereco = dados.endereco();
        this.proprietario = dados.proprietario();
        this.precoVenda = dados.precoVenda();
        this.area = medida("area", "A área", dados.area());
        this.frente = medida("frente", "A frente", dados.frente());
        this.fundo = medida("fundo", "O fundo", dados.fundo());
        this.quartos = contagem("quartos", "O número de quartos", dados.quartos());
        this.suites = contagem("suites", "O número de suítes", dados.suites());
        this.banheiros = contagem("banheiros", "O número de banheiros", dados.banheiros());
        this.vagas = contagem("vagas", "O número de vagas", dados.vagas());
    }

    public void marcarVendido() {
        exigirDisponivel();
        this.situacao = Situacao.VENDIDO;
    }

    private void exigirDisponivel() {
        if (situacao == Situacao.VENDIDO) {
            throw new ImovelVendidoException(id);
        }
    }

    private static BigDecimal medida(String campo, String nome, BigDecimal valor) {
        if (valor != null && valor.signum() <= 0) {
            throw new ValorInvalidoException(campo, nome + " deve ser maior que zero");
        }
        return valor;
    }

    private static Integer contagem(String campo, String nome, Integer valor) {
        if (valor != null && valor < 0) {
            throw new ValorInvalidoException(campo, nome + " não pode ser negativo");
        }
        return valor;
    }

    public record Dados(Tipo tipo, Endereco endereco, BigDecimal precoVenda, Proprietario proprietario,
                        BigDecimal area, BigDecimal frente, BigDecimal fundo,
                        Integer quartos, Integer suites, Integer banheiros, Integer vagas) {
    }
}
