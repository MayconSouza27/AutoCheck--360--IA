package com.autocheck.domain.entity;

import com.autocheck.domain.enums.SeveridadeAvaria;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_avaria_detectada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AvariaDetectada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vistoria_id", nullable = false)
    private Vistoria vistoria;

    @NotBlank(message = "A localização da avaria é obrigatória")
    @Column(nullable = false, length = 100)
    private String localizacao;

    @NotBlank(message = "A descrição da avaria é obrigatória")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "A severidade é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeveridadeAvaria severidade;

    @NotNull(message = "O percentual de confiança é obrigatório")
    @DecimalMin(value = "0.0", message = "A confiança mínima é 0.0")
    @DecimalMax(value = "100.0", message = "A confiança máxima é 100.0")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confianca;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}
