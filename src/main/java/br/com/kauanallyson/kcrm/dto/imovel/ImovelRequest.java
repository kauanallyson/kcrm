package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.model.imovel.Proprietario;
import br.com.kauanallyson.kcrm.model.imovel.Tipo;

import java.math.BigDecimal;

// Características (área, frente, fundo, quartos, suítes, banheiros, vagas) são opcionais
public record ImovelRequest(
        Tipo tipo,
        EnderecoRequest endereco,
        BigDecimal precoVenda,
        ProprietarioRequest proprietario,
        BigDecimal area,
        BigDecimal frente,
        BigDecimal fundo,
        Integer quartos,
        Integer suites,
        Integer banheiros,
        Integer vagas
) {
    public Imovel.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        errors.exigir("tipo", tipo, "O tipo não pode ficar em branco");
        errors.exigir("endereco", endereco, "O endereço não pode ficar em branco");
        errors.exigir("precoVenda", precoVenda, "O preço de venda não pode ficar em branco");
        errors.exigir("proprietario", proprietario, "O Proprietário não pode ficar em branco");
        Endereco enderecoValido = endereco == null ? null : endereco.toEndereco(errors);
        Proprietario proprietarioValido = proprietario == null ? null : proprietario.toProprietario(errors);
        errors.throwIfAny();
        return new Imovel.Dados(tipo, enderecoValido, precoVenda, proprietarioValido,
                area, frente, fundo, quartos, suites, banheiros, vagas);
    }
}
