package com.banco.dto;

import com.banco.model.Auditoria;

import java.time.Instant;

public record AuditoriaResponse(
        Long id,
        String usuarioCpf,
        String acao,
        String entidade,
        Long entidadeId,
        String descricao,
        Instant dataHora
) {
    public static AuditoriaResponse de(Auditoria a) {
        return new AuditoriaResponse(a.getId(), a.getUsuarioCpf(), a.getAcao(), a.getEntidade(),
                a.getEntidadeId(), a.getDescricao(), a.getDataHora());
    }
}
