create table
    if not exists asset_schema (
        id int auto_increment primary key,
        asset_id int,
        version_id int,
        schema_json json,
        schema_hash CHAR(64),
        previous_schema_id int,
        created_at timestamp default CURRENT_TIMESTAMP,
        updated_at timestamp default CURRENT_TIMESTAMP,
        constraint fk_asset_schema_asset_id foreign key (asset_id) references asset (id) on update cascade on delete cascade,
        constraint fk_asset_schema_self_id foreign key (previous_schema_id) references asset_schema (id) on update cascade on delete cascade,
        constraint u_asset_schema_composite_key unique (asset_id, schema_hash),
        index idx_asset_schema_version (version_id),
        index idx_asset_schema_hash (schema_hash)
    );

create table
    if not exists schema_classification (
        id int auto_increment primary key,
        asset_id int,
        schema_id int not null,
        classified_schema_json json,
        created_at timestamp default CURRENT_TIMESTAMP,
        updated_at timestamp default CURRENT_TIMESTAMP,
        constraint fk_classification_asset_id foreign key (asset_id) references asset (id) on update cascade on delete cascade,
        constraint fk_classification_source_schema foreign key (schema_id) references asset_schema (id) on update cascade on delete cascade,
        index idx_classification_source (schema_id),
        constraint u_schema_classification_composite_key unique (asset_id, schema_id)
    );

create table
    if not exists schema_classification_status (
        id int auto_increment primary key,
        schema_classification_id int,
        classification_type varchar(255),
        classification_status varchar(255),
        classification_method varchar(255),
        notification_status varchar(255),
        classified_by varchar(255),
        classified_at timestamp null,
        classification_notes text,
        reviewed_by varchar(255),
        reviewed_at timestamp null,
        review_notes text,
        created_at timestamp default CURRENT_TIMESTAMP,
        updated_at timestamp default CURRENT_TIMESTAMP,
        constraint fk_schema_classification_status_schema_classification_id foreign key (schema_classification_id) references schema_classification (id) on update cascade on delete cascade
    );