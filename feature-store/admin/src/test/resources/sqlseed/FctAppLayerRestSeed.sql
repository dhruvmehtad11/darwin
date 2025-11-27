-- entities --

INSERT
IGNORE INTO `cassandra_store_entity_metadata` (`name`, `entity`, `state`, `owner`, `tags`, `description`, `created_at`,
                                               `updated_at`)
VALUES ('t15',
        '{\"features\":[{\"name\":\"p_col1\",\"tags\":[\"a\",\"b\"],\"type\":\"INT\",\"description\":\"something\"},{\"name\":\"p_col2\",\"tags\":[],\"type\":\"INT\",\"description\":\"\"},{\"name\":\"p_col3\",\"tags\":[],\"type\":\"INT\",\"description\":\"\"}],\"tableName\":\"t15\",\"primaryKeys\":[\"p_col1\",\"p_col2\"]}',
        'LIVE', 'ujjwal.bagrania@dream11.com', '[\"tpp\", \"suggest\"]', 'some-description', '2024-02-20 08:56:47',
        '2024-02-20 08:56:47');

INSERT
IGNORE INTO `cassandra_store_entity_metadata` (`name`, `entity`, `state`, `owner`, `tags`, `description`, `created_at`,
                                               `updated_at`)
VALUES ('t10',
        '{\"features\":[{\"name\":\"p_col1\",\"tags\":[\"a\",\"b\"],\"type\":\"INT\",\"description\":\"something\"},{\"name\":\"p_col2\",\"tags\":[],\"type\":\"INT\",\"description\":\"\"},{\"name\":\"p_col3\",\"tags\":[],\"type\":\"INT\",\"description\":\"\"}],\"tableName\":\"t10\",\"primaryKeys\":[\"p_col1\",\"p_col2\"]}',
        'LIVE', 'ujjwal.bagrania@dream11.com', '[\"tpp\", \"suggest\"]', 'some-description', '2024-02-20 08:56:47',
        '2024-02-20 09:57:08');

-- feature-groups --

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)
VALUES ('f300', 'v1', 'ONLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"f300f1t1\", \"f300f1t2\"], \"type\": \"TEXT\", \"description\": \"something\"},{\"name\": \"col2\", \"tags\": [\"a\", \"b\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\", \"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f300\", \"featureGroupType\": \"ONLINE\"}',
        't15', 'LIVE', 'owner1@dream11.com', '[\"f300t1\", \"f300t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 09:57:08');

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)
VALUES ('f400', 'v1', 'ONLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"f400f1t1\", \"f400f1t2\"], \"type\": \"TEXT\", \"description\": \"something\"},{\"name\": \"col2\", \"tags\": [\"f400f2t1\", \"f400f2t2\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\",\"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f400\", \"featureGroupType\": \"ONLINE\"}',
        't15', 'LIVE', 'owner2@dream11.com', '[\"f400t1\", \"f400t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 09:57:08');

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)
VALUES ('f500', 'v1', 'ONLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"a\", \"b\"], \"type\": \"TEXT\", \"description\": \"something\"}, {\"name\": \"col2\", \"tags\": [\"a\", \"b\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\", \"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f500\", \"featureGroupType\": \"ONLINE\"}',
        't15', 'LIVE', 'owner3@dream11.com', '[\"t1\", \"t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 08:57:08');

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version_enabled`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)
VALUES ('f600', false, 'v1', 'ONLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"f300FakeFeatureName\", \"b\"], \"type\": \"TEXT\", \"description\": \"something\"}, {\"name\": \"col2\", \"tags\": [\"a\", \"b\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\", \"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f600\", \"featureGroupType\": \"ONLINE\", \"versionEnabled\": false}',
        't15', 'LIVE', 'owner4@dream11.com', '[\"t1\", \"t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 08:57:08');

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)
VALUES ('f900', 'v1', 'OFFLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"f900f1t1\", \"f900f1t2\"], \"type\": \"TEXT\", \"description\": \"something\"}, {\"name\": \"col2\", \"tags\": [\"a\", \"b\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\", \"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f400\", \"featureGroupType\": \"OFFLINE\"}',
        't15', 'LIVE', 'owner1@dream11.com', '[\"f900t1\", \"f900t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 09:57:08');

INSERT
IGNORE INTO `cassandra_store_feature_group_metadata` (`name`, `version`, `feature_group_type`, `feature_group`,
                                                      `entity_name`, `state`, `owner`, `tags`, `description`,
                                                      `created_at`, `updated_at`)

VALUES ('f1000', 'v1', 'OFFLINE',
        '{\"features\": [{\"name\": \"col1\", \"tags\": [\"f1000f1t1\", \"f1000f1t2\"], \"type\": \"TEXT\", \"description\":\"something\"},{\"name\": \"col2\", \"tags\": [\"f1000f2t1\", \"f1000f2t2\"], \"type\": \"ASCII\", \"description\": \"something\"}, {\"name\": \"col3\",\"tags\": [\"a\", \"b\"], \"type\": \"VARCHAR\", \"description\": \"something\"}, {\"name\": \"col4\", \"tags\": [\"a\", \"b\"], \"type\": \"BLOB\", \"description\": \"something\"}, {\"name\": \"col5\", \"tags\": [\"a\", \"b\"], \"type\": \"BOOLEAN\", \"description\": \"something\"}, {\"name\": \"col6\", \"tags\": [\"a\", \"b\"], \"type\": \"DECIMAL\", \"description\": \"something\"}, {\"name\": \"col7\", \"tags\": [\"a\", \"b\"], \"type\": \"DOUBLE\", \"description\": \"something\"}, {\"name\": \"col8\", \"tags\": [\"a\", \"b\"], \"type\": \"FLOAT\", \"description\": \"something\"}, {\"name\": \"col9\", \"tags\": [\"a\", \"b\"], \"type\": \"INT\", \"description\": \"something\"}, {\"name\": \"col10\", \"tags\": [\"a\", \"b\"], \"type\": \"BIGINT\", \"description\": \"something\"}, {\"name\": \"col11\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMESTAMP\", \"description\": \"something\"}, {\"name\": \"col12\", \"tags\": [\"a\", \"b\"], \"type\": \"TIMEUUID\", \"description\": \"something\"}, {\"name\": \"col13\", \"tags\": [\"a\", \"b\"], \"type\": \"UUID\", \"description\": \"something\"}, {\"name\": \"col14\", \"tags\": [\"a\", \"b\"], \"type\": \"INET\", \"description\": \"something\"}, {\"name\": \"col15\", \"tags\": [\"a\", \"b\"], \"type\": \"VARINT\", \"description\": \"something\"}], \"entityName\": \"t15\", \"featureGroupName\": \"f400\", \"featureGroupType\": \"OFFLINE\"}',
        't15', 'LIVE', 'owner2@dream11.com', '[\"f1000t1\", \"f1000t2\"]', 'some-description', '2024-02-20 08:57:08',
        '2024-02-20 09:57:08');


-- feature-group-versions --

INSERT
IGNORE INTO `cassandra_store_feature_group_version_map` (`name`, `latest_version`, `updated_at`)
VALUES ('f300', 'v1', '2024-02-20 09:57:13');

INSERT
IGNORE INTO `cassandra_store_feature_group_version_map` (`name`, `latest_version`, `updated_at`)
VALUES ('f400', 'v1', '2024-02-20 09:57:13');

INSERT
IGNORE INTO `cassandra_store_feature_group_version_map` (`name`, `latest_version`, `updated_at`)
VALUES ('f500', 'v1', '2024-02-20 08:57:13');

INSERT
IGNORE INTO `cassandra_store_feature_group_version_map` (`name`, `latest_version`, `updated_at`)
VALUES ('f600', 'v1', '2024-02-20 08:57:13');