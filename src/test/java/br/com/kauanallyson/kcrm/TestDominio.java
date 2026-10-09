package br.com.kauanallyson.kcrm;

import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.common.Telefone;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

// Objetos de domínio prontos para os testes unitários
public final class TestDominio {
    public static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    public static final Telefone WHATSAPP = new Telefone("88999990000");

    private TestDominio() {
    }

    public static Corretor corretor() {
        return Corretor.cadastrar(new Corretor.Dados("Bruno", new Email("bruno@test.com"), "CRECI-CE 1234", WHATSAPP),
                "segredo123", ENCODER);
    }
}
