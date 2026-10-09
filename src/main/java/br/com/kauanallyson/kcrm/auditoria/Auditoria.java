package br.com.kauanallyson.kcrm.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Trilha de auditoria: uma linha por ação relevante no logger "kcrm.auditoria", que tem retenção própria.
// Só ids entram aqui; nunca senha, token, CPF ou telefone
@Component
public class Auditoria {
    public static final String LOGGER = "kcrm.auditoria";

    private static final Logger log = LoggerFactory.getLogger(LOGGER);

    // corretor e recurso podem ser nulos (ex.: falha de login não tem Corretor)
    public void registrar(String acao, UUID corretor, UUID recurso) {
        LoggingEventBuilder evento = log.atInfo()
                .setMessage("acao={} corretor={} recurso={}")
                .addArgument(acao)
                .addArgument(corretor)
                .addArgument(recurso)
                .addKeyValue("acao", acao);
        if (corretor != null) {
            evento = evento.addKeyValue("corretor", corretor.toString());
        }
        if (recurso != null) {
            evento = evento.addKeyValue("recurso", recurso.toString());
        }
        evento.log();
    }
}
