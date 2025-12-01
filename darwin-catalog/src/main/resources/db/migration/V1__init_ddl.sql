create table
    if not exists asset (
        id int auto_increment primary key,
        fqdn varchar(255),
        type enum (
            'TABLE',
            'STREAM',
            'SERVICE',
            'DASHBOARD',
            'KAFKA'
        ) not null,
        description varchar(255),
        source_platform varchar(255),
        business_roster varchar(255),
        current_incident varchar(255),
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        asset_created_at timestamp,
        asset_updated_at timestamp,
        quality_score varchar(255),
        metadata json,
        constraint asset_unique_fqdn unique (fqdn),
        index asset_index_type (type)
    );

create table
    if not exists asset_tag_relation (
        id int auto_increment primary key,
        asset_id int not null,
        tag varchar(255),
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        constraint fk_asset_tag foreign key (asset_id) references asset (id) on delete cascade on update cascade
    );

create table
    if not exists asset_lineage (
        id int auto_increment primary key,
        from_asset_id int not null,
        to_asset_id int not null,
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        constraint fk_asset_lineage_from foreign key (from_asset_id) references asset (id) on delete cascade on update cascade,
        constraint fk_asset_lineage_to foreign key (to_asset_id) references asset (id) on delete cascade on update cascade
    );

create table
    if not exists field_lineage (
        id int auto_increment primary key,
        asset_lineage_id int not null,
        from_field_name varchar(2000) not null,
        to_field_name varchar(2000) not null,
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        constraint fk_field_lineage_asset_ref foreign key (asset_lineage_id) references asset_lineage (id) on delete cascade on update cascade
    );

create table
    if not exists rule (
        id int auto_increment primary key,
        asset_id int not null,
        monitor_id bigint,
        type varchar(255) not null,
        schedule varchar(255),
        left_expression varchar(255),
        comparator varchar(255),
        right_expression varchar(255),
        health_status boolean,
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        slack_channel varchar(255),
        severity varchar(255),
        constraint fk_rule foreign key (asset_id) references asset (id) on delete restrict on update cascade,
        index idx_rule_monitor (monitor_id)
    );

create table
    if not exists asset_directory (
        id int auto_increment primary key,
        asset_name varchar(255),
        asset_prefix varchar(255),
        depth int,
        is_terminal boolean,
        count int default 1,
        sort_path varchar(255),
        created_at timestamp default current_timestamp,
        updated_at timestamp default current_timestamp,
        constraint asset_directory_unique unique (asset_name, asset_prefix),
        index idx_asset_directory_prefix (asset_prefix),
        index idx_asset_directory_sort_path (sort_path)
    );

create table
    if not exists config (
        config_key VARCHAR(100) PRIMARY KEY,
        config_value TEXT,
        value_type VARCHAR(50),
        index idx_config_config_key (config_key)
    );

insert ignore into config (config_key, config_value, value_type)
values
    (
        'glue_database_whitelist',
        '["apxor", "apxor_processed", "clevertap", "clevertap_processed", "company_aggregates", "company_datascience", "company_events", "company_events_processed", "company_gupshup", "company_haptics", "company_intalk", "company_karix", "company_rs_gold", "company_rs_silver", "company_stitch", "company_stitch_plus", "company_transactions", "company_zendesk", "data_stitch_etl", "example_datahighway", "example_transactions", "ds", "event_aggregates", "fantasy_bazaar_lakehouse", "fc_transactions", "redshift_segment_company", "redshift_segment_company_sandbox", "redshift_segment_data_stitch_etl", "redshift_segment_ds", "redshift_segment_example", "redshift_segment_example_rs_gold", "redshift_segment_fc_shop", "redshift_segment_sporta_metrics", "sporta_metrics_lakehouse", "stream_databeam_transactions", "stream_datahighway_events", "stream_datahighway_logger", "stream_de_contest", "stream_de_tier_one", "stream_debezium", "stream_sporta_transactions", "stream_streamverse", "stream_thirdparty_logger", "stream_waf_logger"]',
        'LIST'
    );