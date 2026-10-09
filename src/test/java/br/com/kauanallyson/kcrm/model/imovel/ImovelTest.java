package br.com.kauanallyson.kcrm.model.imovel;

import br.com.kauanallyson.kcrm.TestDominio;
import br.com.kauanallyson.kcrm.exception.ImovelVendidoException;
import br.com.kauanallyson.kcrm.exception.ValorInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImovelTest {
    private static final Corretor CORRETOR = TestDominio.corretor();
    private static final Endereco ENDERECO = Endereco.de("Rua A", "1", null, "Centro", "Sobral", "CE", "62010000");
    private static final Proprietario PROPRIETARIO = new Proprietario("  Ana  ", new Telefone("88999991111"),
            new Email("ana@test.com"), new Cpf("52998224725"));

    private static Imovel.Dados dados(BigDecimal preco) {
        return new Imovel.Dados(Tipo.TERRENO, ENDERECO, preco, PROPRIETARIO,
                null, null, null, null, null, null, null);
    }

    @Test
    void cadastroComecaDisponivelECaracteristicasSaoOpcionais() {
        Imovel imovel = Imovel.cadastrar(dados(new BigDecimal("150000")), CORRETOR);

        assertThat(imovel.getSituacao()).isEqualTo(Situacao.DISPONIVEL);
        assertThat(imovel.getArea()).isNull();
        assertThat(imovel.getQuartos()).isNull();
        assertThat(imovel.getProprietario().nome()).isEqualTo("Ana");
    }

    @Test
    void qualquerTipoAceitaQualquerCaracteristica() {
        Imovel imovel = Imovel.cadastrar(new Imovel.Dados(Tipo.APARTAMENTO, ENDERECO, BigDecimal.TEN, PROPRIETARIO,
                new BigDecimal("80"), new BigDecimal("10"), new BigDecimal("8"), 3, 1, 2, 1), CORRETOR);

        assertThat(imovel.getFrente()).isEqualByComparingTo("10");
        assertThat(imovel.getSuites()).isEqualTo(1);
    }

    @Test
    void precoDeVendaDeveSerPositivo() {
        assertThatThrownBy(() -> Imovel.cadastrar(dados(BigDecimal.ZERO), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("precoVenda");
        assertThatThrownBy(() -> Imovel.cadastrar(dados(null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("precoVenda");
    }

    @Test
    void medidasPositivasEContagensNaoNegativas() {
        assertThatThrownBy(() -> Imovel.cadastrar(new Imovel.Dados(Tipo.CASA, ENDERECO, BigDecimal.TEN, PROPRIETARIO,
                BigDecimal.ZERO, null, null, null, null, null, null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("area");
        assertThatThrownBy(() -> Imovel.cadastrar(new Imovel.Dados(Tipo.CASA, ENDERECO, BigDecimal.TEN, PROPRIETARIO,
                null, null, null, -1, null, null, null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("quartos");
        Imovel semVagas = Imovel.cadastrar(new Imovel.Dados(Tipo.CASA, ENDERECO, BigDecimal.TEN, PROPRIETARIO,
                null, null, null, null, null, null, 0), CORRETOR);
        assertThat(semVagas.getVagas()).isZero();
    }

    @Test
    void vendidoNaoPodeSerEditadoNemVendidoDeNovo() {
        Imovel imovel = Imovel.cadastrar(dados(BigDecimal.TEN), CORRETOR);
        imovel.marcarVendido();

        assertThat(imovel.getSituacao()).isEqualTo(Situacao.VENDIDO);
        assertThatThrownBy(() -> imovel.atualizarDados(dados(BigDecimal.ONE)))
                .isInstanceOf(ImovelVendidoException.class);
        assertThatThrownBy(imovel::marcarVendido).isInstanceOf(ImovelVendidoException.class);
    }

    @Test
    void tipoEnderecoEProprietarioSaoObrigatorios() {
        assertThatThrownBy(() -> Imovel.cadastrar(new Imovel.Dados(null, ENDERECO, BigDecimal.TEN, PROPRIETARIO,
                null, null, null, null, null, null, null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("tipo");
        assertThatThrownBy(() -> Imovel.cadastrar(new Imovel.Dados(Tipo.CASA, null, BigDecimal.TEN, PROPRIETARIO,
                null, null, null, null, null, null, null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("endereco");
        assertThatThrownBy(() -> Imovel.cadastrar(new Imovel.Dados(Tipo.CASA, ENDERECO, BigDecimal.TEN, null,
                null, null, null, null, null, null, null), CORRETOR))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("proprietario");
    }

    @Test
    void proprietarioExigeTodosOsDados() {
        assertThatThrownBy(() -> new Proprietario(" ", new Telefone("88999991111"), new Email("a@test.com"), new Cpf("52998224725")))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("proprietario.nome");
        assertThatThrownBy(() -> new Proprietario("Ana", new Telefone("88999991111"), new Email("a@test.com"), null))
                .isInstanceOf(ValorInvalidoException.class).extracting("campo").isEqualTo("proprietario.cpf");
    }
}
