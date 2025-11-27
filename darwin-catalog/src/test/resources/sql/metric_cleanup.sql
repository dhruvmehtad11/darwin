delete from asset where fqdn in (
    'example:table:redshift:segment:example:contestmaster3',
    'example:table:redshift:segment:example:contestmaster2',
    'example:table:redshift:segment:example:contestmaster1'
    );
delete from asset where source_platform = 'databeam';

-- Cleanup data after DataDog webhook integration tests

-- Delete test rules first (foreign key dependency)
DELETE FROM rule WHERE monitor_id IN (12345, 67890, 11111);

-- Delete test assets
DELETE FROM asset WHERE fqdn = 'example:table:test:webhook:test_asset_1';
DELETE FROM asset WHERE fqdn = 'example:table:test:webhook:test_asset_2';