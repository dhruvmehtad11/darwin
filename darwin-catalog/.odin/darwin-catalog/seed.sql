-- Seed data for Darwin Catalog (aggregated from src/test/resources/sql)

-- lineage.sql
insert into asset.asset (fqdn, type) value ('1_L21', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
insert into asset.asset (fqdn, type) value ('2_L22', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}, {"name": "c3", "type": "string"}, {"name": "c4", "type": "string"}]}',
        'f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f'
    );
insert into asset.asset (fqdn, type) value ('3_L23', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
insert into asset.asset (fqdn, type) value ('4_L11', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '766ac5705748d54fb861a58581582e63d2040a3e8cbaee91ff45e4c5a2e9e1e0'
    );
insert into asset.asset (fqdn, type) value ('5_L12', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
insert into asset.asset (fqdn, type) value ('6_M', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}]}',
        '2006c74f182f528b592c2c09f92850833c5337d294e683859d0e1f1464256afc'
    );
insert into asset.asset (fqdn, type) value ('7_R11', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
insert into asset.asset (fqdn, type) value ('8_R12', 'KAFKA');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}]}',
        '06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d'
    );
insert into asset.asset (fqdn, type) value ('9_R21', 'DASHBOARD');
insert into asset.asset (fqdn, type) value ('10_R22', 'TABLE');
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "c1", "type": "string"}, {"name": "c2", "type": "string"}, {"name": "c3", "type": "string"}, {"name": "c4", "type": "string"}]}',
        'f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f'
    );
insert into asset.asset (fqdn, type) value ('11_R23', 'SERVICE');

-- monitor.sql
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:example:test_x_matchpoints', 'TABLE', 'databeam', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:redshift:segment:example:test_x_matchpoints', 'TABLE', 'databeam', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_events_processed:test_x_matchpoints', 'TABLE', 'datahighway', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_events:test_x_matchpoints', 'TABLE', 'datahighway', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_aggregates:aceapu', 'TABLE', 'datastitch', 'roster1');
insert into asset.asset (fqdn, type, source_platform) value ('example:table:lakehouse:company_aggregates:aceapu1', 'TABLE', 'datastitch');

-- list_search_pagination.sql
insert into asset.asset (fqdn, type) value (
        'example:table:redshift:example:test_x_league',
        'TABLE'
    );
SET @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "dreamid", "type": "int"}, {"name": "leagueid", "type": "int"}, {"name": "createddate", "type": "timestamp"}]}',
        '6f5ba445722971379d3915e8060adef7b8f5cbfacde52849be2c0f1e1050e517'
    );
insert into asset.asset (fqdn, type) value (
        'example:table:redshift:example:test_x_matchpoints',
        'TABLE'
    );
set @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "tourname", "type": "string"}, {"name": "matchno", "type": "int"}, {"name": "dreamid", "type": "int"}]}',
        'c9a6fd1927a51d251e44e4ab21d220d7d40325852e4bed17749bbfa4f7903e14'
    );
insert into asset.asset (fqdn, type) value (
        'example:table:redshift:example:test_x_test',
        'TABLE'
    );
set @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "points", "type": "float"}, {"name": "tourname", "type": "string"}, {"name": "date", "type": "timestamp"}]}',
        '03f6df0d30bf9e15e9f4e39b291d970519960da92d5aa922890f00aadd8dc808'
    );
insert into asset.asset (fqdn, type) value (
        'example:kafka:datahighway_logger:test_consumer_offsets',
        'KAFKA'
    );
set @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "points", "type": "float"}, {"name": "tourname", "type": "string"}, {"name": "date", "type": "timestamp"}]}',
        '03f6df0d30bf9e15e9f4e39b291d970519960da92d5aa922890f00aadd8dc808'
    );
insert into asset.asset (fqdn, type) value (
        'example:kafka:datahighway_logger:test_alleventattributes',
        'KAFKA'
    );
set @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "points", "type": "float"}, {"name": "tourname", "type": "string"}, {"name": "date", "type": "timestamp"}]}',
        '03f6df0d30bf9e15e9f4e39b291d970519960da92d5aa922890f00aadd8dc808'
    );
insert into asset.asset (fqdn, type) value (
        'example:kafka:datahighway_logger:test_alleventattributes_pwa',
        'KAFKA'
    );
set @asset_id = LAST_INSERT_ID();
INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (
        @asset_id,
        1,
        '{"name": "schema", "type": "record", "fields": [{"name": "points", "type": "float"}, {"name": "tourname", "type": "string"}, {"name": "date", "type": "timestamp"}]}',
        '03f6df0d30bf9e15e9f4e39b291d970519960da92d5aa922890f00aadd8dc808'
    );
insert into asset.asset (fqdn, type) value (
        'example:service:cge:test_cge-cashout',
        'SERVICE'
    );
insert into asset.asset (fqdn, type) value ('example:service:cge:test_cge-join', 'SERVICE');
insert into asset.asset (fqdn, type) value (
        'example:dashboard:gsheet:test_vctransactions',
        'DASHBOARD'
    );
insert into asset.asset (fqdn, type) value (
        'example:dashboard:looker:test_1429',
        'DASHBOARD'
    );
insert into asset.asset_directory (asset_name, asset_prefix, depth, is_terminal, count, sort_path)
values  ('example', 'root', 0, 0, 6, 'example:root'),
        ('table', 'example', 1, 0, 3, 'example:table'),
        ('redshift', 'example:table', 2, 0, 3, 'example:table:redshift'),
        ('example', 'example:table:redshift', 3, 0, 3, 'example:table:redshift:example'),
        ('test_x_league', 'example:table:redshift:example', 4, 1, 1, 'example:table:redshift:example:test_x_league'),
        ('test_x_matchpoints', 'example:table:redshift:example', 4, 1, 1, 'example:table:redshift:example:test_x_matchpoints'),
        ('test_x_test', 'example:table:redshift:example', 4, 1, 1, 'example:table:redshift:example:test_x_test'),
        ('kafka', 'example', 1, 0, 3, 'example:kafka'),
        ('datahighway_logger', 'example:kafka', 2, 0, 3, 'example:kafka:datahighway_logger'),
        ('test_consumer_offsets', 'example:kafka:datahighway_logger', 3, 1, 1, 'example:kafka:datahighway_logger:test_consumer_offsets'),
        ('test_alleventattributes', 'example:kafka:datahighway_logger', 3, 1, 1, 'example:kafka:datahighway_logger:test_alleventattributes'),
        ('test_alleventattributes_pwa', 'example:kafka:datahighway_logger', 3, 1, 1, 'example:kafka:datahighway_logger:test_alleventattributes_pwa');

-- metric.sql
insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster1', 'TABLE', null);
insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster2', 'TABLE', 'databeam');
insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster3', 'TABLE', 'databeam');

INSERT INTO asset.asset (fqdn, type, source_platform, quality_score) VALUES
  ('example:table:test:webhook:test_asset_1', 'TABLE', 'test', '2/2'),
  ('example:table:test:webhook:test_asset_2', 'TABLE', 'test', '1/1');

INSERT INTO asset.rule (asset_id, type, comparator, left_expression, right_expression, health_status, monitor_id) VALUES
  ((SELECT MAX(id) - 1 FROM asset.asset), 'FRESHNESS', 'GREATER_THAN', 'freshness_metric', '100.0', true, 12345),
  ((SELECT MAX(id) - 1 FROM asset.asset), 'COMPLETENESS', 'LESS_THAN', 'completeness_metric', '95.0', true, 67890),
  ((SELECT MAX(id) FROM asset.asset), 'CORRECTNESS', 'GREATER_THAN', 'correctness_metric', '99.0', true, 11111);

-- put_schema.sql
INSERT INTO asset.asset (fqdn, type, description, source_platform, business_roster, asset_created_at, asset_updated_at) VALUES
    ('example:table:redshift:segment:example:test_existing_asset', 'TABLE', 'Test asset for schema operations', 'databeam', 'data_team', NOW(), NOW()),
    ('example:table:redshift:segment:example:test_asset_with_schema', 'TABLE', 'Test asset that already has a schema', 'databeam', 'data_team', NOW(), NOW()),
    ('example:table:redshift:segment:example:test_asset_with_no_schema', 'TABLE', 'Test asset that already has a schema', 'databeam', 'data_team', NOW(), NOW());

SET @asset_with_schema_id = (SELECT id FROM asset.asset WHERE fqdn = 'example:table:redshift:segment:example:test_asset_with_schema');

INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash) VALUES
    (@asset_with_schema_id, 1, '{"type": "record", "name": "existing_schema", "fields": [{"name": "existing_field", "type": "varchar", "doc": "Existing field from seed data", "is_pii": false}]}', 'existing_hash_v1');

-- register_asset.sql
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

SET @existing_asset_id = LAST_INSERT_ID();

INSERT INTO asset.asset_schema (asset_id, version_id, schema_json, schema_hash)
VALUES (@existing_asset_id, 1, '{"name": "schema", "type": "record", "fields": [{"name": "transaction_id", "type": "bigint"}, {"name": "user_id", "type": "bigint"}, {"name": "amount", "type": "decimal"}, {"name": "created_at", "type": "timestamp"}]}', 'b36dd10ea7babb61444058c13942a2e84b6870165fbfc210dacda4c9aa7764ce');

INSERT INTO asset.asset_tag_relation (asset_id, tag) VALUES
    (@existing_asset_id, 'financial'),
    (@existing_asset_id, 'pii'),
    (@existing_asset_id, 'production');
