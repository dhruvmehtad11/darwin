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
-- ====================================================================================
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
-- ====================================================================================
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
-- ====================================================================================
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
-- ====================================================================================
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
-- ====================================================================================
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
-- ====================================================================================
insert into asset.asset (fqdn, type) value (
        'example:service:cge:test_cge-cashout',
        'SERVICE'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value ('example:service:cge:test_cge-join', 'SERVICE');
-- ====================================================================================
insert into asset.asset (fqdn, type) value (
        'example:dashboard:gsheet:test_vctransactions',
        'DASHBOARD'
    );
-- ====================================================================================
insert into asset.asset (fqdn, type) value (
        'example:dashboard:looker:test_1429',
        'DASHBOARD'
    );
-- ======= update search directory
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
