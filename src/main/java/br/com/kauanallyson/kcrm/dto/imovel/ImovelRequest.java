package br.com.kauanallyson.kcrm.dto.imovel;

import br.com.kauanallyson.kcrm.dto.common.EnderecoRequest;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.FieldErrors;
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
    // Formatos e regras do Imóvel voltam todos juntos
    public Imovel.Dados toDados() {
        FieldErrors errors = new FieldErrors();
        Endereco enderecoValido = endereco == null ? null : endereco.toEndereco(errors);
        Proprietario proprietarioValido = proprietario == null ? null : proprietario.toProprietario(errors);
        Imovel.Dados dados = errors.collect(() -> new Imovel.Dados(tipo, enderecoValido, precoVenda,
                proprietarioValido, area, frente, fundo, quartos, suites, banheiros, vagas));
        errors.throwIfAny();
        return dados;
    }
}
