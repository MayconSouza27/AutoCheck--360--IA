package com.autocheck.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_veiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A placa do veículo é obrigatória")
    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @NotBlank(message = "O modelo é obrigatório")
    @Column(nullable = false, length = 100)
    private String modelo;

    @NotBlank(message = "A marca é obrigatória")
    @Column(nullable = false, length = 50)
    private String marca;

    @NotNull(message = "O ano é obrigatório")
    @Column(nullable = false)
    private Integer ano;

    @Column(length = 30)
    private String cor;

    @Column(unique = true, length = 30)
    private String chassi;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}
