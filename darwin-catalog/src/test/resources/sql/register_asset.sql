-- Seed data for register asset integration tests

-- Insert an existing asset to test duplicate registration
INSERT INTO asset.asset (fqdn, type, description, source_platform, business_roster, asset_created_at, asset_updated_at) 
VALUES (
    'example:table:redshift:segment:example:existing_transactions', 
    'TABLE', 
    'Existing transactions table for testing duplicates',
    'databeam',
    'financial_team',
    NOW(),
    NOW()
);

INSERT INTO asset.asset (fqdn, type, description, source_platform, business_roster, asset_created_at, asset_updated_at)
VALUES (
           'example:table:redshift:segment:example:existing_transactions_2',
           'TABLE',
           'Existing transactions table for testing duplicates',
           'databeam',
           'financial_team',
           NOW(),
           NOW()
       );

-- Get the asset ID for the existing asset
SET @existing_asset_id = LAST_INSERT_ID();

-- Insert some fields for the existing asset
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (@existing_asset_id, 1, '{"name": "schema", "type": "record", "fields": [{"name": "transaction_id", "type": "bigint"}, {"name": "user_id", "type": "bigint"}, {"name": "amount", "type": "decimal"}, {"name": "created_at", "type": "timestamp"}]}', 'b36dd10ea7babb61444058c13942a2e84b6870165fbfc210dacda4c9aa7764ce');

-- Insert some tags for the existing asset
INSERT INTO asset.asset_tag_relation (asset_id, tag) VALUES
    (@existing_asset_id, 'financial'),
    (@existing_asset_id, 'pii'),
    (@existing_asset_id, 'production');

-- Update search directory for the existing asset
INSERT INTO asset.asset_directory (asset_name, asset_prefix, depth, is_terminal, count, sort_path)
VALUES  
    ('example', 'root', 0, 0, 1, 'example:root'),
    ('table', 'example', 1, 0, 1, 'example:table'),
    ('redshift', 'example:table', 2, 0, 1, 'example:table:redshift'),
    ('segment', 'example:table:redshift', 3, 0, 1, 'example:table:redshift:segment'),
    ('example', 'example:table:redshift:segment', 4, 0, 1, 'example:table:redshift:segment:example'),
    ('existing_transactions', 'example:table:redshift:segment:example', 5, 1, 1, 'example:table:redshift:segment:example:existing_transactions');