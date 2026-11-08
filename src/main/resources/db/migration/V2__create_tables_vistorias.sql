-- ============================================================
-- Migration V2: Criação das tabelas de Vistoria e Avarias
-- ============================================================

CREATE TABLE tb_vistoria (
    id BIGSERIAL PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    data_vistoria TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(30) NOT NULL,
    status_analise VARCHAR(30) NOT NULL,
    url_foto VARCHAR(500),
    criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_vistoria_veiculo FOREIGN KEY (veiculo_id)
        REFERENCES tb_veiculo (id) ON DELETE CASCADE
);

CREATE TABLE tb_avaria_detectada (
    id BIGSERIAL PRIMARY KEY,
    vistoria_id BIGINT NOT NULL,
    localizacao VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    severidade VARCHAR(20) NOT NULL,
    confianca NUMERIC(5, 2) NOT NULL,
    criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avaria_vistoria FOREIGN KEY (vistoria_id) 
        REFERENCES tb_vistoria (id) ON DELETE CASCADE
);

-- Índices para otimizar pesquisas
CREATE INDEX idx_vistoria_veiculo_id ON tb_vistoria (veiculo_id);
CREATE INDEX idx_avaria_vistoria_id ON tb_avaria_detectada (vistoria_id);
