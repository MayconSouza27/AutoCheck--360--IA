package com.autocheck.controller;

import com.autocheck.domain.enums.StatusAnalise;
import com.autocheck.domain.enums.TipoVistoria;
import com.autocheck.domain.repository.UsuarioRepository;
import com.autocheck.domain.service.VistoriaService;
import com.autocheck.dto.response.VistoriaResponseDTO;
import com.autocheck.infrastructure.security.SecurityConfigurations;
import com.autocheck.infrastructure.security.SecurityFilter;
import com.autocheck.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VistoriaController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class})
class VistoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VistoriaService vistoriaService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private final MockMultipartFile mockImagem = new MockMultipartFile(
            "imagem",
            "carro.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "bytes-falsos-de-imagem".getBytes()
    );

    @Test
    @DisplayName("Deve retornar 401 Unauthorized ao tentar analisar vistoria sem token JWT")
    void deveRetornar401QuandoRequisicaoSemTokenJwt() throws Exception {
        mockMvc.perform(multipart("/api/v1/vistorias/1/analisar")
                        .file(mockImagem)
                        .param("tipo", "ENTRADA"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized ao enviar token JWT expirado ou inválido")
    void deveRetornar401QuandoTokenJwtInvalido() throws Exception {
        when(tokenService.validarToken("token-invalido")).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/vistorias/1/analisar")
                        .file(mockImagem)
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "vistoriador@autocheck.com", roles = {"VISTORIADOR"})
    @DisplayName("Deve retornar 201 Created quando usuário autenticado com perfil ROLE_VISTORIADOR enviar vistoria")
    void deveRetornar201QuandoUsuarioAutenticadoComPerfilVistoriador() throws Exception {
        VistoriaResponseDTO responseDTO = new VistoriaResponseDTO(
                1L,
                10L,
                LocalDateTime.now(),
                TipoVistoria.ENTRADA,
                StatusAnalise.CONCLUIDA,
                "carro.jpg",
                List.of()
        );

        when(vistoriaService.analisarVistoria(eq(10L), eq(TipoVistoria.ENTRADA), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(multipart("/api/v1/vistorias/10/analisar")
                        .file(mockImagem)
                        .param("tipo", "ENTRADA"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.veiculoId").value(10L))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.statusAnalise").value("CONCLUIDA"));
    }

    @Test
    @WithMockUser(username = "locadora@autocheck.com", roles = {"LOCADORA"})
    @DisplayName("Deve retornar 200 OK ao consultar vistoria com perfil ROLE_LOCADORA")
    void deveRetornar200AoConsultarVistoriaComPerfilLocadora() throws Exception {
        VistoriaResponseDTO responseDTO = new VistoriaResponseDTO(
                5L,
                20L,
                LocalDateTime.now(),
                TipoVistoria.SAIDA,
                StatusAnalise.CONCLUIDA,
                "foto.jpg",
                List.of()
        );

        when(vistoriaService.buscarPorId(5L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/vistorias/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.veiculoId").value(20L))
                .andExpect(jsonPath("$.tipo").value("SAIDA"));
    }
}
