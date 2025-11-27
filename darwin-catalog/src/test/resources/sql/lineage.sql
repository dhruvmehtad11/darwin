insert into asset.asset (fqdn, type) value ('1_L21', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('2_L22', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}, {"name": "c3", "type": "string"}, {"name": "c4", "type": "string"}]}',
        'f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('3_L23', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('4_L11', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '766ac5705748d54fb861a58581582e63d2040a3e8cbaee91ff45e4c5a2e9e1e0'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('5_L12', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('6_M', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}]}',
        '2006c74f182f528b592c2c09f92850833c5337d294e683859d0e1f1464256afc'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('7_R11', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('8_R12', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('9_R21', 'DASHBOARD');
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('10_R22', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}, {"name": "c3", "type": "string"}, {"name": "c4", "type": "string"}]}',
        'f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('11_R23', 'SERVICE');
