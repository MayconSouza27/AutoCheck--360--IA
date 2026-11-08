-- ============================================================
-- Migration V4: Inserção de veículos de demonstração (Frota para Locadoras e Logística)
-- ============================================================
INSERT INTO tb_veiculo (placa, modelo, marca, ano, cor, chassi) VALUES 
('BRA2E19', 'Chevrolet Onix 1.0 Flex', 'Chevrolet', 2023, 'Branco Summit', '9BGKS48T0PG123456'),
('RIO2A18', 'Fiat Mobi Like 1.0', 'Fiat', 2024, 'Prata Bari', '9BD18128BPG654321'),
('SPX9F88', 'Hyundai HB20 Sense 1.0', 'Hyundai', 2023, 'Preto Onix', '9BHBG51DAPG987654')
ON CONFLICT (placa) DO NOTHING;
