-- Fase 3: alinhar schema com dominio/API fechado
-- Nota: migration incremental para nao quebrar ambientes onde V1/V2 ja correram.

-- 1) app_user: suporte a autenticacao por PIN/NFC
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS pin_hash VARCHAR(255);

ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS nfc_uid VARCHAR(128);

-- Unicidade de nfc_uid apenas quando preenchido
CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_nfc_uid_not_null
    ON app_user (nfc_uid)
    WHERE nfc_uid IS NOT NULL;

-- 2) audit_log: novo modelo actor_type + actor_ref
ALTER TABLE audit_log
    ADD COLUMN IF NOT EXISTS actor_type VARCHAR(32);

ALTER TABLE audit_log
    ADD COLUMN IF NOT EXISTS actor_ref VARCHAR(128);

-- Backfill minimo para manter dados antigos coerentes
UPDATE audit_log
SET actor_type = COALESCE(actor_type, 'SYSTEM'),
    actor_ref = COALESCE(actor_ref, actor);

ALTER TABLE audit_log
    ALTER COLUMN actor_type SET NOT NULL;

-- 3) tool: RFID globalmente unico
ALTER TABLE tool
    DROP CONSTRAINT IF EXISTS uq_tool_cabinet_tag;

ALTER TABLE tool
    ADD CONSTRAINT uq_tool_tag_code_global UNIQUE (tag_code);

-- 4) inventory_snapshot_item: suportar tags desconhecidas
ALTER TABLE inventory_snapshot_item
    ALTER COLUMN tool_id DROP NOT NULL;

ALTER TABLE inventory_snapshot_item
    ADD COLUMN IF NOT EXISTS tag_code VARCHAR(128);

ALTER TABLE inventory_snapshot_item
    ADD COLUMN IF NOT EXISTS recognized BOOLEAN;

-- Backfill minimo para linhas antigas (sem perdas)
UPDATE inventory_snapshot_item
SET recognized = COALESCE(recognized, TRUE)
WHERE recognized IS NULL;

ALTER TABLE inventory_snapshot_item
    ALTER COLUMN recognized SET NOT NULL;

-- Mantem not null em tag_code apos fase de transicao:
-- se ja existirem dados, primeiro tens de preencher tag_code por join com tool.
-- Se ainda nao tens dados, podes ativar diretamente.
-- ALTER TABLE inventory_snapshot_item
--     ALTER COLUMN tag_code SET NOT NULL;

ALTER TABLE inventory_snapshot_item
    DROP CONSTRAINT IF EXISTS uq_inventory_snapshot_item_snapshot_tool;

-- Unicidade por snapshot + tag (novo contrato)
CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_snapshot_item_snapshot_tag
    ON inventory_snapshot_item (snapshot_id, tag_code);
