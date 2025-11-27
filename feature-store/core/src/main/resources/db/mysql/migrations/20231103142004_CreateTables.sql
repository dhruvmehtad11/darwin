CREATE TABLE IF NOT EXISTS cassandra_store_entity_metadata
(
    name        VARCHAR(200) NOT NULL,
    entity      JSON         NOT NULL,
    state       ENUM
                    (
                        'LIVE',
                        'ARCHIVED'
                        ) DEFAULT 'LIVE',
    owner       VARCHAR(200) NOT NULL,
    tags        JSON,
    description TEXT         NOT NULL,
    created_at  DATETIME  DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner),
    PRIMARY KEY (name)
);
# Todo: add state 'Locked'

CREATE TABLE IF NOT EXISTS cassandra_store_feature_group_metadata
(
    name               VARCHAR(200) NOT NULL,
    version            VARCHAR(200) NOT NULL,
    version_enabled    BOOLEAN DEFAULT TRUE,
    feature_group_type ENUM
                           (
                               'ONLINE',
                               'OFFLINE'
                               ) DEFAULT 'ONLINE',
    feature_group      JSON         NOT NULL,
    entity_name        VARCHAR(200) NOT NULL,
    state              ENUM
                           (
                               'LIVE',
                               'ARCHIVED'
                               ) DEFAULT 'LIVE',
    owner              VARCHAR(200) NOT NULL,
    tags               JSON,
    description        TEXT         NOT NULL,
    created_at         DATETIME  DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner),
    INDEX idx_entity_name (entity_name),
    PRIMARY KEY (name, version)
);

ALTER TABLE cassandra_store_feature_group_metadata ADD tenant_config JSON
  DEFAULT ('{"read":"default-tenant","write":"default-tenant","consume":"default-tenant"}');


CREATE TABLE IF NOT EXISTS cassandra_store_feature_group_version_map
(
    name           VARCHAR(200) NOT NULL,
    latest_version VARCHAR(200) NOT NULL,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);


## search helpers ##

CREATE TABLE IF NOT EXISTS cassandra_store_entity_tag_map
(
    tag_name    VARCHAR(200) NOT NULL,
    entity_name VARCHAR(200) NOT NULL,
    INDEX index_tag_name (tag_name),
    PRIMARY KEY (tag_name, entity_name)
);

CREATE TABLE IF NOT EXISTS cassandra_store_feature_group_tag_map
(
    tag_name              VARCHAR(200) NOT NULL,
    feature_group_name    VARCHAR(200) NOT NULL,
    feature_group_version VARCHAR(200) NOT NULL,
    INDEX index_tag_name (tag_name),
    PRIMARY KEY (tag_name, feature_group_name, feature_group_version)
);


CREATE TABLE IF NOT EXISTS cassandra_store_entity_feature_map
(
    entity_name         VARCHAR(200) NOT NULL,
    feature_column_name VARCHAR(200) NOT NULL,
    feature_column_type VARCHAR(200) NOT NULL,
    description         VARCHAR(200) NOT NULL,
    tags                JSON,
    INDEX index_entity_name (entity_name),
    PRIMARY KEY (entity_name, feature_column_name)
);

CREATE TABLE IF NOT EXISTS cassandra_store_feature_group_feature_map
(
    feature_group_name    VARCHAR(200) NOT NULL,
    feature_group_version VARCHAR(200) NOT NULL,
    feature_column_name   VARCHAR(200) NOT NULL,
    feature_column_type   VARCHAR(200) NOT NULL,
    description           VARCHAR(200) NOT NULL,
    tags                  JSON,
    INDEX index_feature_group_name_version (feature_group_name, feature_group_version),
    PRIMARY KEY (feature_group_name, feature_group_version, feature_column_name)
);

CREATE TABLE IF NOT EXISTS cassandra_store_tenant_consumer_map
(
  tenant_name           VARCHAR(200) NOT NULL,
  topic_name            VARCHAR(200) NOT NULL,
  num_partitions        INT NOT NULL,
  num_consumers         INT NOT NULL,
  updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tenant_name)
);

CREATE TABLE IF NOT EXISTS cassandra_store_feature_group_run_data
(
  name               VARCHAR(200) NOT NULL,
  version            VARCHAR(200) NOT NULL,
  run_id             VARCHAR(200) NOT NULL,
  source             VARCHAR(1024) DEFAULT '', # for sdk v2
  time_taken         BIGINT,
  row_count          BIGINT,
  sample_data        JSON,
  status             ENUM ('SUCCESS', 'FAILED'),
  error_message      VARCHAR(1024) DEFAULT '',
  created_at         DATETIME  DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (name, version, run_id)
);

ALTER TABLE cassandra_store_feature_group_run_data ADD COLUMN timestamp BIGINT DEFAULT (UNIX_TIMESTAMP());

CREATE TABLE IF NOT EXISTS cassandra_store_tenant_populator_map
(
  tenant_name           VARCHAR(200) NOT NULL,
  num_workers           INT NOT NULL,
  updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tenant_name)
);