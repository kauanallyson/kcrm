package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.dto.common.FieldErrors;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.imovel.Imovel;
import br.com.kauanallyson.kcrm.model.imovel.Proprietario;
import br.com.kauanallyson.kcrm.model.imovel.Tipo;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Características (área, frente, fundo, quartos, suítes, banheiros, vagas) são opcionais
public record ImovelRequest(
        @NotNull(message = "O tipo não pode ficar em branco")
        Tipo tipo,
        @NotNull(message = "O endereço não pode ficar em branco")
        EnderecoRequest endereco,
        @NotNull(message = "O preço de venda não pode ficar em branco")
        BigDecimal precoVenda,
        @NotNull(message = "O Proprietário não pode ficar em branco")
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
        Endereco enderecoValido = endereco.toEndereco(errors);
        Proprietario proprietarioValido = proprietario.toProprietario(errors);
        errors.throwIfAny();
        return new Imovel.Dados(tipo, enderecoValido, precoVenda, proprietarioValido,
                area, frente, fundo, quartos, suites, banheiros, vagas);
    }
}
