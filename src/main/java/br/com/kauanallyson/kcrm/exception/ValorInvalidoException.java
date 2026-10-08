package br.com.kauanallyson.kcrm.exception;

// Lançada pelos tipos de valor; campo é o nome do campo no JSON, para o erro voltar por campo
public class ValorInvalidoException extends IllegalArgumentException {
    private final String campo;

    public ValorInvalidoException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
