package br.com.kauanallyson.kcrm.dto.common;

import br.com.kauanallyson.kcrm.model.common.Cep;
import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Uf;

// As regras de formato ficam nos tipos de valor; aqui só se coletam os erros por campo
public record EnderecoRequest(
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String cep
) {
    public Endereco toEndereco(FieldErrors errors) {
        Uf uf = errors.collect(() -> Uf.daSigla(estado));
        Cep cepValido = errors.collect(() -> new Cep(cep));
        // Com estado ou CEP inválidos, o construtor ainda aponta os erros dos demais campos
        return errors.collect(() -> new Endereco(rua, numero, complemento, bairro, cidade, uf, cepValido));
    }
}
