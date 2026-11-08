package com.autocheck.infrastructure.gemini;

import com.autocheck.infrastructure.gemini.dto.GeminiAnaliseResultDTO;
import com.autocheck.infrastructure.gemini.dto.GeminiRequestDTO;
import com.autocheck.infrastructure.gemini.dto.GeminiResponseDTO;
import com.autocheck.infrastructure.gemini.exception.GeminiIntegrationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class GeminiVisionClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    private static final String PROMPT_ANALISE_VISTORIA = """
        Você é um perito automotivo sênior e analista especialista em vistoria veicular.
        Sua tarefa é inspecionar minuciosamente a imagem fornecida do veículo e detectar todas as avarias visíveis (como arranhões, amassados, trincas em vidros, faróis quebrados, desgastes ou peças danificadas).

        Diretrizes Obrigatórias:
        1. Retorne estritamente um JSON com a chave raiz "avarias" no formato:
        {
          "avarias": [
            {
              "localizacao": "Local exato (Ex: Para-choque dianteiro direito, Porta traseira esquerda, Farol dianteiro)",
              "descricao": "Descrição técnica clara e objetiva do dano observado",
              "severidade": "LEVE",
              "confianca": 92.50
            }
          ]
        }
        2. O campo severidade DEVE ser estritamente um dos seguintes valores em maiúsculo: LEVE, MEDIA ou GRAVE.
        3. O campo confianca deve ser um número decimal de 0.00 a 100.00 representando a probabilidade da detecção.
        4. Se nenhuma avaria for detectada no veículo, retorne exatamente: {"avarias": []}.
        5. Não adicione nenhum comentário adicional, apenas o JSON solicitado.
        """;

    public GeminiVisionClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}") String apiUrl
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public GeminiAnaliseResultDTO analisarImagem(MultipartFile imagem) {
        validarImagem(imagem);

        try {
            log.info("Iniciando processamento de imagem para vistoria. Nome: {}, Tamanho: {} bytes",
                    imagem.getOriginalFilename(), imagem.getSize());

            String mimeType = imagem.getContentType() != null ? imagem.getContentType() : "image/jpeg";
            String base64Data = Base64.getEncoder().encodeToString(imagem.getBytes());

            GeminiRequestDTO requestPayload = new GeminiRequestDTO(
                    List.of(
                            new GeminiRequestDTO.Content(
                                    List.of(
                                            GeminiRequestDTO.Part.fromText(PROMPT_ANALISE_VISTORIA),
                                            GeminiRequestDTO.Part.fromImageData(mimeType, base64Data)
                                    )
                            )
                    ),
                    new GeminiRequestDTO.GenerationConfig("application/json", 0.2)
            );

            log.debug("Enviando requisição para a API do Google Gemini...");

            GeminiResponseDTO response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String errorBody = new String(res.getBody().readAllBytes());
                        log.error("Erro retornado pela API do Gemini. Status: {}, Body: {}", res.getStatusCode(), errorBody);
                        throw new GeminiIntegrationException("Falha na comunicação com a API do Gemini: " + res.getStatusCode() + " - " + errorBody);
                    })
                    .body(GeminiResponseDTO.class);

            if (response == null || response.getFirstTextResponse() == null) {
                log.warn("Resposta vazia recebida do Gemini Vision.");
                return new GeminiAnaliseResultDTO(List.of());
            }

            String rawJson = response.getFirstTextResponse();
            return deserializarResultado(rawJson);

        } catch (IOException e) {
            log.error("Erro ao ler os bytes da imagem enviada", e);
            throw new GeminiIntegrationException("Erro ao processar arquivo de imagem para vistoria", e);
        } catch (GeminiIntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado durante a análise visual com Gemini", e);
            throw new GeminiIntegrationException("Erro inesperado na chamada ao Gemini Vision: " + e.getMessage(), e);
        }
    }

    private GeminiAnaliseResultDTO deserializarResultado(String rawJson) {
        try {
            String jsonLimpo = limparFormatacaoJson(rawJson);
            JsonNode root = objectMapper.readTree(jsonLimpo);

            // Caso a IA tenha retornado diretamente o array [ { ... } ]
            if (root.isArray()) {
                List<GeminiAnaliseResultDTO.AvariaItemDTO> items = objectMapper.readerForListOf(GeminiAnaliseResultDTO.AvariaItemDTO.class)
                        .readValue(root);
                return new GeminiAnaliseResultDTO(items);
            }

            // Caso padrão { "avarias": [ ... ] }
            return objectMapper.treeToValue(root, GeminiAnaliseResultDTO.class);

        } catch (Exception e) {
            log.error("Falha ao desserializar JSON retornado pela IA. Conteúdo bruto: {}", rawJson, e);
            throw new GeminiIntegrationException("Falha ao interpretar resultado estruturado da IA: " + e.getMessage(), e);
        }
    }

    private String limparFormatacaoJson(String rawText) {
        if (rawText == null) return "{}";
        String trimmed = rawText.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de imagem da vistoria não pode ser nulo ou vazio.");
        }
        String contentType = imagem.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("O arquivo fornecido não é uma imagem válida (Content-Type: " + contentType + ").");
        }
    }
}
