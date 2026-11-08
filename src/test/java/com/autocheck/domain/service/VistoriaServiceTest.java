package com.autocheck.domain.service;

import com.autocheck.domain.entity.Vistoria;
import com.autocheck.domain.enums.SeveridadeAvaria;
import com.autocheck.domain.enums.StatusAnalise;
import com.autocheck.domain.enums.TipoVistoria;
import com.autocheck.domain.exception.ResourceNotFoundException;
import com.autocheck.domain.repository.VeiculoRepository;
import com.autocheck.domain.repository.VistoriaRepository;
import com.autocheck.dto.response.VistoriaResponseDTO;
import com.autocheck.infrastructure.gemini.GeminiVisionClient;
import com.autocheck.infrastructure.gemini.dto.GeminiAnaliseResultDTO;
import com.autocheck.infrastructure.gemini.exception.GeminiIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VistoriaServiceTest {

    @Mock
    private VistoriaRepository vistoriaRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private GeminiVisionClient geminiVisionClient;

    @InjectMocks
    private VistoriaService vistoriaService;

    private MockMultipartFile mockImagem;

    @BeforeEach
    void setUp() {
        mockImagem = new MockMultipartFile(
                "imagem",
                "carro_vistoria.jpg",
                "image/jpeg",
                "dados-imagem-falsa".getBytes()
        );
    }

    @Test
    @DisplayName("Deve analisar a vistoria com sucesso quando a IA detectar avarias")
    void deveAnalisarVistoriaComSucessoQuandoIaDetectarAvarias() {
        // Arrange
        Long veiculoId = 1L;
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);

        GeminiAnaliseResultDTO mockIaResponse = new GeminiAnaliseResultDTO(
                List.of(
                        new GeminiAnaliseResultDTO.AvariaItemDTO(
                                "Para-choque dianteiro",
                                "Arranhão profundo na lateral",
                                SeveridadeAvaria.MEDIA,
                                new BigDecimal("94.50")
                        ),
                        new GeminiAnaliseResultDTO.AvariaItemDTO(
                                "Farol esquerdo",
                                "Lente trincada",
                                SeveridadeAvaria.GRAVE,
                                new BigDecimal("98.00")
                        )
                )
        );
        when(geminiVisionClient.analisarImagem(mockImagem)).thenReturn(mockIaResponse);

        when(vistoriaRepository.save(any(Vistoria.class))).thenAnswer(invocation -> {
            Vistoria vistoriaASalvar = invocation.getArgument(0);
            vistoriaASalvar.setId(10L);
            vistoriaASalvar.setDataVistoria(LocalDateTime.now());
            return vistoriaASalvar;
        });

        // Act
        VistoriaResponseDTO resultado = vistoriaService.analisarVistoria(veiculoId, TipoVistoria.ENTRADA, mockImagem);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.id());
        assertEquals(veiculoId, resultado.veiculoId());
        assertEquals(TipoVistoria.ENTRADA, resultado.tipo());
        assertEquals(StatusAnalise.CONCLUIDA, resultado.statusAnalise());
        assertEquals(2, resultado.avarias().size());

        assertEquals("Para-choque dianteiro", resultado.avarias().get(0).localizacao());
        assertEquals(SeveridadeAvaria.MEDIA, resultado.avarias().get(0).severidade());
        assertEquals(new BigDecimal("94.50"), resultado.avarias().get(0).confianca());

        verify(veiculoRepository, times(1)).existsById(veiculoId);
        verify(geminiVisionClient, times(1)).analisarImagem(mockImagem);
        verify(vistoriaRepository, times(1)).save(any(Vistoria.class));
    }

    @Test
    @DisplayName("Deve salvar vistoria com status CONCLUIDA e sem avarias quando o veículo estiver íntegro")
    void deveSalvarVistoriaSemAvariasQuandoIaNaoDetectarDanos() {
        // Arrange
        Long veiculoId = 2L;
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(geminiVisionClient.analisarImagem(mockImagem)).thenReturn(new GeminiAnaliseResultDTO(List.of()));

        when(vistoriaRepository.save(any(Vistoria.class))).thenAnswer(invocation -> {
            Vistoria vistoria = invocation.getArgument(0);
            vistoria.setId(20L);
            vistoria.setDataVistoria(LocalDateTime.now());
            return vistoria;
        });

        // Act
        VistoriaResponseDTO resultado = vistoriaService.analisarVistoria(veiculoId, TipoVistoria.SAIDA, mockImagem);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusAnalise.CONCLUIDA, resultado.statusAnalise());
        assertTrue(resultado.avarias().isEmpty());
        verify(vistoriaRepository, times(1)).save(any(Vistoria.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o veículo não existir e não chamar a IA")
    void deveLancarResourceNotFoundExceptionQuandoVeiculoNaoExistir() {
        // Arrange
        Long veiculoInexistenteId = 999L;
        when(veiculoRepository.existsById(veiculoInexistenteId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> vistoriaService.analisarVistoria(veiculoInexistenteId, TipoVistoria.PERIODICA, mockImagem)
        );

        assertTrue(exception.getMessage().contains("Veículo não encontrado"));
        verifyNoInteractions(geminiVisionClient);
        verify(vistoriaRepository, never()).save(any(Vistoria.class));
    }

    @Test
    @DisplayName("Deve propagar GeminiIntegrationException e abortar a persistência quando a IA falhar")
    void devePropagarGeminiIntegrationExceptionQuandoIaFalhar() {
        // Arrange
        Long veiculoId = 1L;
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(geminiVisionClient.analisarImagem(mockImagem))
                .thenThrow(new GeminiIntegrationException("Timeout na API do Gemini"));

        // Act & Assert
        GeminiIntegrationException exception = assertThrows(
                GeminiIntegrationException.class,
                () -> vistoriaService.analisarVistoria(veiculoId, TipoVistoria.PERIODICA, mockImagem)
        );

        assertEquals("Timeout na API do Gemini", exception.getMessage());
        verify(vistoriaRepository, never()).save(any(Vistoria.class));
    }
}
