package com.autocheck.dto.request;

import com.autocheck.domain.enums.TipoVistoria;
import jakarta.validation.constraints.NotNull;

public record VistoriaCreateRequestDTO(
    @NotNull(message = "O tipo da vistoria é obrigatório")
    TipoVistoria tipo
) {}
