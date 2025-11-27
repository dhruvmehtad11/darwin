#!/usr/bin/env bash
set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"

RESOURCES_PATH="./resources/"

mysql -u ${DARWIN_MYSQL_USERNAME} -p${DARWIN_MYSQL_PASSWORD} -h ${DARWIN_MYSQL_HOST} -P 3306 -e "CREATE DATABASE IF NOT EXISTS darwin;" && \
mysql -u ${DARWIN_MYSQL_USERNAME} -p${DARWIN_MYSQL_PASSWORD} -h ${DARWIN_MYSQL_HOST} -P 3306 < resources/db/mysql/migrations/20230627145332_CreateTables.sql

# For production like environments only check if migrations are already run. We intend to run migrations manually or via mydbops
if [[ "$ENV" == prod* ]] || [[ "$ENV" == uat* ]]; then
  migrationsCmd=$checkMigrationsCmd
else
  migrationsCmd=$runMigrationsCmd

  #  Create index for ES
  echo "Creating index computea_v2"
  curl -sf -o /dev/null -I "${DARWIN_ELASTICSEARCH_HOST}:9200/computea_v2" || \
  curl --location --request PUT "${DARWIN_ELASTICSEARCH_HOST}:9200/computea_v2"

  # Create mappings for computea_v2 index
  echo "Creating mappings"
  curl --location --request PUT "${DARWIN_ELASTICSEARCH_HOST}:9200/computea_v2/_mappings" --header 'Content-Type: application/json' --data-raw '{"dynamic_templates": [{"labels_string_fields": {"path_match": "labels.*", "match_mapping_type": "string", "mapping": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}], "properties": {"active_pods": {"type": "long"}, "advance_config": {"properties": {"availability_zone": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "env_variables": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "init_script": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "instance_role": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "log_path": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "ray_start_params": {"properties": {"num_cpus_on_head": {"type": "long"}, "num_gpus_on_head": {"type": "long"}, "object_store_memory_perc": {"type": "long"}}}, "spark_config": {"properties": {"spark": {"properties": {"ui": {"properties": {"proxyBase": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "proxyRedirectUri": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}}}}}}}, "auto_termination_policies": {"properties": {"enabled": {"type": "boolean"}, "params": {"properties": {"head_node_cpu_usage_threshold": {"type": "long"}, "worker_node_cpu_usage_threshold": {"type": "long"}}}, "policy_name": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}, "cloud_env": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "cluster_id": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "created_on": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "head_node": {"properties": {"node": {"properties": {"cores": {"type": "long"}, "memory": {"type": "long"}, "node_capacity_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "node_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}, "node_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}, "last_used": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "name": {"type": "keyword", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "runtime": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "status": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "tags": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "labels": {"type": "object"}, "labels_flat": {"type": "keyword"}, "terminate_after_minutes": {"type": "long"}, "user": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "worker_group": {"properties": {"max_pods": {"type": "long"}, "min_pods": {"type": "long"}, "node": {"properties": {"cores": {"type": "long"}, "disk": {"properties": {"disk_size": {"type": "long"}, "disk_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}, "memory": {"type": "long"}, "node_capacity_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}, "node_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}, "node_type": {"type": "text", "fields": {"keyword": {"type": "keyword", "ignore_above": 256}}}}}}}'
fi

(eval "${migrationsCmd}")
