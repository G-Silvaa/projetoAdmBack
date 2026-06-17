-- ============================================================================
-- Seed de usuários de demonstração (um por nível de acesso), vinculados ao
-- escritório padrão (empresa_id = 1).
--
-- Senha de todos: arctech123
--
-- ⚠️ SEGURANÇA: estes são usuários de DEMONSTRAÇÃO com senha conhecida.
--    Antes de ir para produção, troque as senhas ou remova estes acessos.
-- ============================================================================

INSERT INTO liv.usuarios (empresa_id, nome, email, senha_hash, nivel, ativo, criado_em, atualizado_em)
VALUES
    (1, 'Administrador Demo', 'admin@arctech.com.br',      '$2a$10$K20JHRWI/sp4OYDSgBHc5OQOaiyuR/2tQ03imA5oOfHn2.77ClBTm', 'ADMINISTRADOR', TRUE, now(), now()),
    (1, 'Gestor Demo',        'gestor@arctech.com.br',     '$2a$10$Bx75Q8pNUl.2wzZFvk74pe6NvXbQaNr8ynB.FHStzXg7Ou9zIzZjS', 'GESTOR',        TRUE, now(), now()),
    (1, 'Operador Demo',      'operador@arctech.com.br',   '$2a$10$YtSW2Js2GY81z2C/M1YxKuT2mf3EAvCWhrGKD0q8P6XrETWk3rM7m', 'OPERADOR',      TRUE, now(), now()),
    (1, 'Financeiro Demo',    'financeiro@arctech.com.br', '$2a$10$V0SOVFBFoHRmbe8jqlLNpOzyUWzPf3CzhZFQK9v6pseoITpUlfNOC', 'FINANCEIRO',    TRUE, now(), now()),
    (1, 'Consulta Demo',      'consulta@arctech.com.br',   '$2a$10$bY7GaZMGQU36KW42bkDAxubXvuZ9bZU7Kd5dywEJoDiLMIrKhgjKa', 'CONSULTA',      TRUE, now(), now())
ON CONFLICT (email) DO NOTHING;
