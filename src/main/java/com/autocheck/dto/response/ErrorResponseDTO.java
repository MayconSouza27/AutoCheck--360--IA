package com.autocheck.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path,
    List<String> details
) {
    public static ErrorResponseDTO of(Integer status, String error, String message, String path) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, List.of());
    }

    public static ErrorResponseDTO of(Integer status, String error, String message, String path, List<String> details) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, error, message, path, details);
    }
}
