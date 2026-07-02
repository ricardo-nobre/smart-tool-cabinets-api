INSERT INTO cabinet (id, code, name, location, api_key_hash, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'CAB-001', 'Cabinet 001', 'Lab A', 'dae68e3b4d111160cb7d41596b384820ff33650bad4b211f27d0fd5e430fd7b7', TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    location = EXCLUDED.location,
    api_key_hash = EXCLUDED.api_key_hash,
    active = EXCLUDED.active;

INSERT INTO app_user (id, username, full_name, role, active, pin_hash, nfc_uid)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'admin', 'Admin LEIRT', 'ADMIN', TRUE, NULL, NULL),
    ('00000000-0000-0000-0000-000000000201', 'operator1', 'Operator Demo', 'OPERATOR', TRUE, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'NFC-OP-001'),
    ('00000000-0000-0000-0000-000000000301', 'supervisor1', 'Supervisor Demo', 'SUPERVISOR', TRUE, NULL, 'NFC-SUP-001')
ON CONFLICT (username) DO UPDATE
SET full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    active = EXCLUDED.active,
    pin_hash = EXCLUDED.pin_hash,
    nfc_uid = EXCLUDED.nfc_uid;

INSERT INTO tool (id, cabinet_id, tag_code, display_name, active)
VALUES
    ('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000001', 'TAG-001', 'Demo screwdriver', TRUE),
    ('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000001', 'TAG-002', 'Demo wrench', TRUE),
    ('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000001', 'TAG-003', 'Demo pliers', TRUE),
    ('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000001', 'TAG-004', 'Demo multimeter', TRUE)
ON CONFLICT (tag_code) DO UPDATE
SET cabinet_id = EXCLUDED.cabinet_id,
    display_name = EXCLUDED.display_name,
    active = EXCLUDED.active;
