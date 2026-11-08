package com.autocheck.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiRequestDTO(
    List<Content> contents,
    @JsonProperty("generationConfig") GenerationConfig generationConfig
) {
    public record Content(List<Part> parts) {}

    public record Part(
        String text,
        @JsonProperty("inline_data") InlineData inlineData
    ) {
        public static Part fromText(String text) {
            return new Part(text, null);
        }

        public static Part fromImageData(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }
    }

    public record InlineData(
        @JsonProperty("mime_type") String mimeType,
        String data
    ) {}

    public record GenerationConfig(
        @JsonProperty("response_mime_type") String responseMimeType,
        Double temperature
    ) {}
}
