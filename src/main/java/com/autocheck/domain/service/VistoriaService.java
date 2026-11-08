package com.autocheck.domain.service;

import com.autocheck.domain.entity.AvariaDetectada;
import com.autocheck.domain.entity.Vistoria;
import com.autocheck.domain.enums.StatusAnalise;
import com.autocheck.domain.enums.TipoVistoria;
import com.autocheck.domain.exception.ResourceNotFoundException;
import com.autocheck.domain.repository.VeiculoRepository;
import com.autocheck.domain.repository.VistoriaRepository;
import com.autocheck.dto.response.VistoriaResponseDTO;
import com.autocheck.infrastructure.gemini.GeminiVisionClient;
import com.autocheck.infrastructure.gemini.dto.GeminiAnaliseResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VistoriaService {

    private final VistoriaRepository vistoriaRepository;
    private final VeiculoRepository veiculoRepository;
    private final GeminiVisionClient geminiVisionClient;

    // Injeção de dependência via construtor (Inversão de Controle)
    public VistoriaService(
            VistoriaRepository vistoriaRepository,
            VeiculoRepository veiculoRepository,
            GeminiVisionClient geminiVisionClient
    ) {
        this.vistoriaRepository = vistoriaRepository;
        this.veiculoRepository = veiculoRepository;
        this.geminiVisionClient = geminiVisionClient;
    }

    @Transactional
    public VistoriaResponseDTO analisarVistoria(Long veiculoId, TipoVistoria tipo, MultipartFile imagem) {
        log.info("Iniciando processo de vistoria para o veículo ID: {}, Tipo: {}", veiculoId, tipo);

        // 1. Validação de existência do veículo
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new ResourceNotFoundException("Veículo não encontrado com o ID: " + veiculoId);
        }

        // 2. Criação da vistoria com status EM_PROCESSAMENTO
        Vistoria vistoria = Vistoria.builder()
                .veiculoId(veiculoId)
                .dataVistoria(LocalDateTime.now())
                .tipo(tipo != null ? tipo : TipoVistoria.PERIODICA)
                .statusAnalise(StatusAnalise.EM_PROCESSAMENTO)
                .urlFoto(imagem.getOriginalFilename())
                .build();

        // 3. Chamada da IA Generativa (Gemini Vision) para inspeção visual
        GeminiAnaliseResultDTO analiseIA = geminiVisionClient.analisarImagem(imagem);

        // 4. Cruzamento e mapeamento das avarias detectadas
        if (analiseIA != null && analiseIA.avarias() != null && !analiseIA.avarias().isEmpty()) {
            log.info("Avarias identificadas pela IA: {} avarias encontradas.", analiseIA.avarias().size());

            analiseIA.avarias().forEach(item -> {
                AvariaDetectada avaria = AvariaDetectada.builder()
                        .localizacao(item.localizacao())
                        .descricao(item.descricao())
                        .severidade(item.severidade())
                        .confianca(item.confianca())
                        .build();

                // Vincula avaria à vistoria mantendo a integridade bidirecional
                vistoria.adicionarAvaria(avaria);
            });
        } else {
            log.info("Nenhuma avaria detectada na imagem pela IA. Veículo em conformidade.");
        }

        // 5. Atualização do status da vistoria para CONCLUIDA
        vistoria.setStatusAnalise(StatusAnalise.CONCLUIDA);

        // 6. Persistência atômica no banco de dados (Vistoria + Avarias em cascata)
        Vistoria vistoriaSalva = vistoriaRepository.save(vistoria);
        log.info("Vistoria ID: {} salva com sucesso no banco de dados.", vistoriaSalva.getId());

        return VistoriaResponseDTO.fromEntity(vistoriaSalva);
    }

    @Transactional(readOnly = true)
    public VistoriaResponseDTO buscarPorId(Long id) {
        Vistoria vistoria = vistoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vistoria não encontrada com o ID: " + id));
        return VistoriaResponseDTO.fromEntity(vistoria);
    }

    @Transactional(readOnly = true)
    public List<VistoriaResponseDTO> listarPorVeiculo(Long veiculoId) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new ResourceNotFoundException("Veículo não encontrado com o ID: " + veiculoId);
        }
        return vistoriaRepository.findByVeiculoId(veiculoId).stream()
                .map(VistoriaResponseDTO::fromEntity)
                .toList();
    }
}
