-- Minimal demonstrator data for local MVP validation.

INSERT INTO cabinet (id, code, name, location, api_key_hash, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'CAB-001', 'Cabinet 001', 'Lab A', NULL, TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    location = EXCLUDED.location,
    active = EXCLUDED.active;

INSERT INTO app_user (id, username, full_name, role, active, pin_hash, nfc_uid)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'admin', 'Admin LEIRT', 'ADMIN', TRUE, NULL, NULL),
    ('00000000-0000-0000-0000-000000000201', 'operator1', 'Operator Demo', 'OPERATOR', TRUE, '1234', 'NFC-OP-001'),
    ('00000000-0000-0000-0000-000000000301', 'supervisor1', 'Supervisor Demo', 'SUPERVISOR', TRUE, NULL, 'NFC-SUP-001')
ON CONFLICT (username) DO UPDATE
SET full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    active = EXCLUDED.active,
    pin_hash = EXCLUDED.pin_hash,
    nfc_uid = EXCLUDED.nfc_uid;

INSERT INTO tool (id, cabinet_id, tag_code, display_name, type_code, serial_number, active)
VALUES
    ('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000001', 'TAG-001', 'Demo screwdriver', 1, 'SN-TAG-001', TRUE),
    ('00000000-0000-0000-0000-000000001002', '00000000-0000-0000-0000-000000000001', 'TAG-002', 'Demo wrench', 1, 'SN-TAG-002', TRUE),
    ('00000000-0000-0000-0000-000000001003', '00000000-0000-0000-0000-000000000001', 'TAG-003', 'Demo pliers', 1, 'SN-TAG-003', TRUE),
    ('00000000-0000-0000-0000-000000001004', '00000000-0000-0000-0000-000000000001', 'TAG-004', 'Demo multimeter', 2, 'SN-TAG-004', TRUE)
ON CONFLICT (tag_code) DO UPDATE
SET cabinet_id = EXCLUDED.cabinet_id,
    display_name = EXCLUDED.display_name,
    type_code = EXCLUDED.type_code,
    serial_number = EXCLUDED.serial_number,
    active = EXCLUDED.active;
