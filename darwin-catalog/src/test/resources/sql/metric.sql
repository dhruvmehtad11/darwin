insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster1', 'TABLE', null);
insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster2', 'TABLE', 'databeam');
insert into asset.asset (fqdn, type, source_platform) value ('example:table:redshift:segment:example:contestmaster3', 'TABLE', 'databeam');

-- Setup data for DataDog webhook integration tests

-- Insert test assets (let IDs auto-generate)
INSERT INTO asset (fqdn, type, source_platform, quality_score) VALUES
                                                                   ('example:table:test:webhook:test_asset_1', 'TABLE', 'test', '2/2'),
                                                                   ('example:table:test:webhook:test_asset_2', 'TABLE', 'test', '1/1');

-- Insert test rules with specific asset_ids and monitor_ids
-- Using correct column names from the rule table schema
INSERT INTO rule (asset_id, type, comparator, left_expression, right_expression, health_status, monitor_id) VALUES
                                                                                                                ((SELECT MAX(id) - 1 FROM asset), 'FRESHNESS', 'GREATER_THAN', 'freshness_metric', '100.0', true, 12345),
                                                                                                                ((SELECT MAX(id) - 1 FROM asset), 'COMPLETENESS', 'LESS_THAN', 'completeness_metric', '95.0', true, 67890),
                                                                                                                ((SELECT MAX(id) FROM asset), 'CORRECTNESS', 'GREATER_THAN', 'correctness_metric', '99.0', true, 11111);