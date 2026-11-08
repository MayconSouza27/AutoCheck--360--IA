package com.autocheck.dto.response;

import com.autocheck.domain.entity.AvariaDetectada;
import com.autocheck.domain.enums.SeveridadeAvaria;

import java.math.BigDecimal;

public record AvariaResponseDTO(
    Long id,
    String localizacao,
    String descricao,
    SeveridadeAvaria severidade,
    BigDecimal confianca
) {
    public static AvariaResponseDTO fromEntity(AvariaDetectada entity) {
        return new AvariaResponseDTO(
            entity.getId(),
            entity.getLocalizacao(),
            entity.getDescricao(),
            entity.getSeveridade(),
            entity.getConfianca()
        );
    }
}
