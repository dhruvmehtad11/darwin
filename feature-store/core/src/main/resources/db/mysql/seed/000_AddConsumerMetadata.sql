INSERT IGNORE INTO `cassandra_store_tenant_consumer_map` (`tenant_name`, `topic_name`, `num_partitions`, `num_consumers`)
VALUES ('t1', 't1', 1, 1);

INSERT IGNORE INTO `cassandra_store_tenant_consumer_map` (`tenant_name`, `topic_name`, `num_partitions`, `num_consumers`)
VALUES ('t2', 't2', 1, 1);

INSERT IGNORE INTO `cassandra_store_tenant_consumer_map` (`tenant_name`, `topic_name`, `num_partitions`, `num_consumers`)
VALUES ('default-tenant', 'default', 1, 1);

-- populator --
INSERT IGNORE INTO `cassandra_store_tenant_populator_map` (`tenant_name`, `num_workers`)
VALUES ('default-tenant', 8);