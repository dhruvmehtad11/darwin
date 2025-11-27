-- Seed data for PUT schema integration tests

-- Insert test assets without schemas initially
INSERT INTO asset.asset (fqdn, type, description, source_platform, business_roster, asset_created_at, asset_updated_at) VALUES
    ('example:table:redshift:segment:example:test_existing_asset', 'TABLE', 'Test asset for schema operations', 'databeam', 'data_team', NOW(), NOW()),
    ('example:table:redshift:segment:example:test_asset_with_schema', 'TABLE', 'Test asset that already has a schema', 'databeam', 'data_team', NOW(), NOW()),
    ('example:table:redshift:segment:example:test_asset_with_no_schema', 'TABLE', 'Test asset that already has a schema', 'databeam', 'data_team', NOW(), NOW());

-- Get the asset ID for the asset that should have an existing schema
SET @asset_with_schema_id = (SELECT id FROM asset.asset WHERE fqdn = 'example:table:redshift:segment:example:test_asset_with_schema');

-- Insert an existing schema for the second asset (version 1)
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash) VALUES
    (@asset_with_schema_id, 1, '{"type": "record", "name": "existing_schema", "fields": [{"name": "existing_field", "type": "varchar", "doc": "Existing field from seed data", "is_pii": false}]}', 'existing_hash_v1');
