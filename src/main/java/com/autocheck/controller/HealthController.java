package com.autocheck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servico", "AutoCheck 360° - Vistoria Inteligente com IA",
                "versao", "1.0.0",
                "timestamp", LocalDateTime.now().toString(),
                "documentacao", "https://github.com/MayconSouza27/autocheck-360"
        ));
    }
}
