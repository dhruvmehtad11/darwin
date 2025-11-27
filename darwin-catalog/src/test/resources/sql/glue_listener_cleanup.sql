delete from asset
    where fqdn in (
        'example:table:lakehouse:mock-db-1:mock-table-1',
        'example:table:redshift:segment:company_transactions:mock-table-1',
        'example:table:redshift:segment:company_transactions:mock-table-2',
        'example:table:redshift:segment:company_transactions:mock-table-3',
        'example:table:lakehouse:mock-db-2:mock-table-2',
        'example:kafka:data_highway:mock-stream-1',
        'example:table:lakehouse:example_org_transactions:mock-table-1');
delete from asset.asset_directory;
