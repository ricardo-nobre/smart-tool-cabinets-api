-- Seed tecnico minimo para ambiente local.
-- TODO: substituir por seed alinhado aos dados reais de validacao.

INSERT INTO cabinet (id, code, name, location, api_key_hash, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'CAB-001', 'Cabinet 001', 'Lab A', NULL, TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO app_user (id, username, full_name, role, active)
VALUES ('00000000-0000-0000-0000-000000000101', 'admin', 'Admin LEIRT', 'ADMIN', TRUE)
ON CONFLICT (username) DO NOTHING;

