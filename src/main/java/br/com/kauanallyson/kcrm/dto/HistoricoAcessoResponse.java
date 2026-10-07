package br.com.kauanallyson.kcrm.dto;

import br.com.kauanallyson.kcrm.model.HistoricoAcesso;
import br.com.kauanallyson.kcrm.model.TipoMovimentacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HistoricoAcessoResponse(
        TipoMovimentacao tipo,
        String motivo,
        UUID adminId,
        String adminNome,
        OffsetDateTime ocorridoEm
) {
    public static HistoricoAcessoResponse from(HistoricoAcesso historico) {
        return new HistoricoAcessoResponse(historico.getTipo(), historico.getMotivo(),
                historico.getAdmin().getId(), historico.getAdmin().getNome(), historico.getOcorridoEm());
    }
}
