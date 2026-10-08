package br.com.kauanallyson.kcrm.dto.common;

import br.com.kauanallyson.kcrm.model.common.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
        @NotBlank
        String rua,
        @NotBlank
        String numero,
        String complemento,
        @NotBlank
        String bairro,
        @NotBlank
        String cidade,
        @NotBlank
        @Pattern(regexp = "(?i)^\\s*(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)\\s*$",
                message = "deve ser a sigla de uma UF, como CE ou SP")
        String estado,
        @NotBlank
        @Pattern(regexp = "^[^0-9]*([0-9][^0-9]*){8}$", message = "deve ter 8 dígitos: 00000-000")
        String cep
) {
    public Endereco toEndereco() {
        return Endereco.de(rua, numero, complemento, bairro, cidade, estado, cep);
    }
}
