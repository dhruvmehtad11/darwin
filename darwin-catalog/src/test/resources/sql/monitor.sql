insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:example:test_x_matchpoints', 'TABLE', 'databeam', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:redshift:segment:example:test_x_matchpoints', 'TABLE', 'databeam', 'roster1');

insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_events_processed:test_x_matchpoints', 'TABLE', 'datahighway', 'roster1');
insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_events:test_x_matchpoints', 'TABLE', 'datahighway', 'roster1');

insert into asset.asset (fqdn, type, source_platform, business_roster) value ('example:table:lakehouse:company_aggregates:aceapu', 'TABLE', 'datastitch', 'roster1');
insert into asset.asset (fqdn, type, source_platform) value ('example:table:lakehouse:company_aggregates:aceapu1', 'TABLE', 'datastitch');