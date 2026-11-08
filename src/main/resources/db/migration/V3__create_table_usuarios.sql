-- ============================================================
-- Migration V3: Criação da tabela de Usuários e Perfis de Acesso
-- ============================================================
CREATE TABLE tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON tb_usuario (email);

-- Inserção de usuários iniciais (Senha padrão codificada com BCrypt: '123456')
-- Hash: $2a$10$wK1RkM4g8JvQ1OqvqUoiJ.hYh1K4XjQyvYwN3LqL.t5m1d.Jd6P/y
INSERT INTO tb_usuario (nome, email, senha, perfil) VALUES 
('Vistoriador Maycon', 'vistoriador@autocheck.com', '$2a$10$wK1RkM4g8JvQ1OqvqUoiJ.hYh1K4XjQyvYwN3LqL.t5m1d.Jd6P/y', 'ROLE_VISTORIADOR'),
('Gestor Locadora', 'locadora@autocheck.com', '$2a$10$wK1RkM4g8JvQ1OqvqUoiJ.hYh1K4XjQyvYwN3LqL.t5m1d.Jd6P/y', 'ROLE_LOCADORA');
