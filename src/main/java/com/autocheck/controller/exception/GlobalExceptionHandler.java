package com.autocheck.controller.exception;

import com.autocheck.domain.exception.ResourceNotFoundException;
import com.autocheck.dto.response.ErrorResponseDTO;
import com.autocheck.infrastructure.gemini.exception.GeminiIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeminiIntegrationException.class)
    public ResponseEntity<ErrorResponseDTO> handleGeminiIntegrationException(
            GeminiIntegrationException ex,
            HttpServletRequest request
    ) {
        log.error("Falha na integração com a IA Generativa: {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_GATEWAY; // 502 Bad Gateway
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Bad Gateway - Instabilidade na IA",
                "O serviço de inteligência visual está temporariamente indisponível ou retornou uma resposta inesperada: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());

        HttpStatus status = HttpStatus.NOT_FOUND; // 404 Not Found
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Argumento inválido na requisição: {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Request
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Bad Request - Parâmetro Inválido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422 Unprocessable Entity
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Validation Error",
                "Erros de validação nos campos informados.",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE; // 413 Payload Too Large
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Payload Too Large",
                "O tamanho da imagem enviada ultrapassa o limite máximo permitido (15MB).",
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Erro interno não tratado no servidor", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        ErrorResponseDTO error = ErrorResponseDTO.of(
                status.value(),
                "Internal Server Error",
                "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.",
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
