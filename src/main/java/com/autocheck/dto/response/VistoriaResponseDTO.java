package com.autocheck.dto.response;

import com.autocheck.domain.entity.Vistoria;
import com.autocheck.domain.enums.StatusAnalise;
import com.autocheck.domain.enums.TipoVistoria;

import java.time.LocalDateTime;
import java.util.List;

public record VistoriaResponseDTO(
    Long id,
    Long veiculoId,
    LocalDateTime dataVistoria,
    TipoVistoria tipo,
    StatusAnalise statusAnalise,
    String urlFoto,
    List<AvariaResponseDTO> avarias
) {
    public static VistoriaResponseDTO fromEntity(Vistoria entity) {
        List<AvariaResponseDTO> avariasDTO = entity.getAvarias() == null ? List.of() :
            entity.getAvarias().stream()
                .map(AvariaResponseDTO::fromEntity)
                .toList();

        return new VistoriaResponseDTO(
            entity.getId(),
            entity.getVeiculoId(),
            entity.getDataVistoria(),
            entity.getTipo(),
            entity.getStatusAnalise(),
            entity.getUrlFoto(),
            avariasDTO
        );
    }
}
