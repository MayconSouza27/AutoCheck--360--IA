package com.autocheck.controller;

import com.autocheck.domain.enums.TipoVistoria;
import com.autocheck.domain.service.VistoriaService;
import com.autocheck.dto.response.VistoriaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vistorias")
public class VistoriaController {

    private final VistoriaService vistoriaService;

    // Injeção de dependência via construtor
    public VistoriaController(VistoriaService vistoriaService) {
        this.vistoriaService = vistoriaService;
    }

    @PostMapping(
            value = "/{veiculoId}/analisar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VistoriaResponseDTO> analisarVistoria(
            @PathVariable Long veiculoId,
            @RequestParam("imagem") MultipartFile imagem,
            @RequestParam(value = "tipo", required = false, defaultValue = "PERIODICA") TipoVistoria tipo
    ) {
        VistoriaResponseDTO response = vistoriaService.analisarVistoria(veiculoId, tipo, imagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VistoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        VistoriaResponseDTO response = vistoriaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<VistoriaResponseDTO>> listarPorVeiculo(@PathVariable Long veiculoId) {
        List<VistoriaResponseDTO> response = vistoriaService.listarPorVeiculo(veiculoId);
        return ResponseEntity.ok(response);
    }
}
