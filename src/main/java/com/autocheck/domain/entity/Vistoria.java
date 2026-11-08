package com.autocheck.domain.entity;

import com.autocheck.domain.enums.StatusAnalise;
import com.autocheck.domain.enums.TipoVistoria;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_vistoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Vistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O ID do veículo é obrigatório")
    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @NotNull(message = "A data da vistoria é obrigatória")
    @Column(name = "data_vistoria", nullable = false)
    private LocalDateTime dataVistoria;

    @NotNull(message = "O tipo de vistoria é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVistoria tipo;

    @NotNull(message = "O status da análise é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_analise", nullable = false, length = 30)
    private StatusAnalise statusAnalise;

    @Column(name = "url_foto", length = 500)
    private String urlFoto;

    @OneToMany(mappedBy = "vistoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvariaDetectada> avarias = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public void adicionarAvaria(AvariaDetectada avaria) {
        this.avarias.add(avaria);
        avaria.setVistoria(this);
    }
}
