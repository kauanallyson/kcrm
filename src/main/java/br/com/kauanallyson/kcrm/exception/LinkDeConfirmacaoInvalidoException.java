package br.com.kauanallyson.kcrm.exception;

// Expirado, já usado ou inexistente: a resposta é a mesma, e a conta não é confirmada
public class LinkDeConfirmacaoInvalidoException extends DomainException {
    public LinkDeConfirmacaoInvalidoException() {
        super(ErrorCode.LINK_DE_CONFIRMACAO_INVALIDO,
                "Link de confirmação inválido ou expirado; peça um novo link");
    }
}
