package br.com.kauanallyson.kcrm.model.historico;

import br.com.kauanallyson.kcrm.model.common.Cpf;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.usuario.Perfil;
import br.com.kauanallyson.kcrm.model.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventoTest {
    private static final Usuario AUTOR = Usuario.cadastrar(
            new Usuario.Dados("Admin", new Cpf("52998224725"), new Email("admin@test.com"), "1", "a"),
            Perfil.ADMIN, "segredo123", PasswordEncoderFactories.createDelegatingPasswordEncoder());

    @Test
    void eventoGuardaRegistroAutorMotivoEDetalhe() {
        UUID registroId = UUID.randomUUID();

        Evento evento = Evento.registrar(TipoEvento.DESATIVACAO, TipoRegistro.USUARIO, registroId,
                AUTOR, "  Saiu da imobiliária ", "detalhe");

        assertThat(evento.getTipo()).isEqualTo(TipoEvento.DESATIVACAO);
        assertThat(evento.getRegistroTipo()).isEqualTo(TipoRegistro.USUARIO);
        assertThat(evento.getRegistroId()).isEqualTo(registroId);
        assertThat(evento.getAutor()).isSameAs(AUTOR);
        assertThat(evento.getMotivo()).isEqualTo("Saiu da imobiliária");
        assertThat(evento.getDetalhe()).isEqualTo("detalhe");
    }

    @Test
    void motivoEDetalheSaoOpcionais() {
        Evento evento = Evento.registrar(TipoEvento.REATIVACAO, TipoRegistro.USUARIO, UUID.randomUUID(),
                AUTOR, "  ", null);

        assertThat(evento.getMotivo()).isNull();
        assertThat(evento.getDetalhe()).isNull();
    }

    @Test
    void eventoExigeAutor() {
        assertThatThrownBy(() -> Evento.registrar(TipoEvento.REATIVACAO, TipoRegistro.USUARIO, UUID.randomUUID(),
                null, null, null)).isInstanceOf(NullPointerException.class);
    }
}
