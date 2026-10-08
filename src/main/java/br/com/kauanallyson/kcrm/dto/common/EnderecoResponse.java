package br.com.kauanallyson.kcrm.dto.common;

import br.com.kauanallyson.kcrm.model.common.Endereco;
import br.com.kauanallyson.kcrm.model.common.Uf;

public record EnderecoResponse(String rua, String numero, String complemento, String bairro,
                               String cidade, Uf estado, String cep) {
    public static EnderecoResponse from(Endereco endereco) {
        return new EnderecoResponse(endereco.rua(), endereco.numero(), endereco.complemento(),
                endereco.bairro(), endereco.cidade(), endereco.estado(), endereco.cep().value());
    }
}
