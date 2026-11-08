package com.autocheck.infrastructure.gemini.dto;

import com.autocheck.domain.enums.SeveridadeAvaria;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiAnaliseResultDTO(
    List<AvariaItemDTO> avarias
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AvariaItemDTO(
        String localizacao,
        String descricao,
        SeveridadeAvaria severidade,
        BigDecimal confianca
    ) {}
}
