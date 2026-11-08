package com.autocheck.dto.response;

public record LoginResponseDTO(
    String token,
    String tipo,
    Long expiraEmHoras
) {
    public static LoginResponseDTO of(String token) {
        return new LoginResponseDTO(token, "Bearer", 24L);
    }
}
