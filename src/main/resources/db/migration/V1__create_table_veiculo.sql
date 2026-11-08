-- ============================================================
-- Migration V1: Criação da tabela de Veículos
-- ============================================================
CREATE TABLE tb_veiculo (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    modelo VARCHAR(100) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    ano INT NOT NULL,
    cor VARCHAR(30),
    chassi VARCHAR(30) UNIQUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_veiculo_placa ON tb_veiculo (placa);
