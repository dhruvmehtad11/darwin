-- Remove fields for all inserted assets
DELETE FROM asset.field_lineage;

-- Remove assets
DELETE FROM asset.asset
WHERE
	fqdn IN (
		'example:table:redshift:example:test_x_league',
		'example:table:redshift:example:test_x_matchpoints',
		'example:table:redshift:example:test_x_test',
		'example:kafka:datahighway_logger:test_consumer_offsets',
		'example:kafka:datahighway_logger:test_alleventattributes',
		'example:kafka:datahighway_logger:test_alleventattributes_pwa',
		'example:service:cge:test_cge-cashout',
		'example:service:cge:test_cge-join',
		'example:dashboard:gsheet:test_vctransactions',
		'example:dashboard:looker:test_1429'
	);

-- Remove asset directories
DELETE FROM asset.asset_directory
WHERE
	asset_name IN (
		'example',
		'table',
		'redshift',
		'example',
		'test_x_league',
		'test_x_matchpoints',
		'test_x_test',
		'kafka',
		'datahighway_logger',
		'test_consumer_offsets',
		'test_alleventattributes',
		'test_alleventattributes_pwa'
	);