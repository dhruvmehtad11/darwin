#!/bin/sh

YAML_FILE="services.yaml"

# Source the config.env file
set -o allexport
. config.env
set +o allexport

# Variables
APP_DIR="/app"
ENV="darwin-local"
TEAM_SUFFIX="-darwin-local"
VPC_SUFFIX="-darwin-local"
DEPLOYMENT_TYPE="container"
DARWIN_MYSQL_HOST="darwin-mysql"
DARWIN_CASSANDRA_HOST="darwin-cassandra"
DARWIN_MYSQL_USERNAME="root"
DARWIN_MYSQL_PASSWORD="password"


run_pre_deploy_pod() {
if [ $# -lt 3 ]; then
    echo "Usage: run_test_pod <namespace> <image> <service_name> [EXTRA_ENV=VALUE ...]"
    return 1
  fi

  namespace=$1
  image=$2
  service_name=$3
  shift 3
  # Store remaining args for extra env vars

  pod_name="test-pod"

  # Start env JSON array with predefined vars
  env_json="
    {\"name\":\"APP_DIR\",\"value\":\"$APP_DIR\"},
    {\"name\":\"ENV\",\"value\":\"$ENV\"},
    {\"name\":\"TEAM_SUFFIX\",\"value\":\"$TEAM_SUFFIX\"},
    {\"name\":\"VPC_SUFFIX\",\"value\":\"$VPC_SUFFIX\"},
    {\"name\":\"DEPLOYMENT_TYPE\",\"value\":\"$DEPLOYMENT_TYPE\"},
    {\"name\":\"DARWIN_MYSQL_HOST\",\"value\":\"$DARWIN_MYSQL_HOST\"},
    {\"name\":\"DARWIN_CASSANDRA_HOST\",\"value\":\"$DARWIN_CASSANDRA_HOST\"},
    {\"name\":\"DARWIN_MYSQL_USERNAME\",\"value\":\"$DARWIN_MYSQL_USERNAME\"},
    {\"name\":\"DARWIN_MYSQL_PASSWORD\",\"value\":\"$DARWIN_MYSQL_PASSWORD\"},
    {\"name\":\"SERVICE_NAME\",\"value\":\"$service_name\"}
  "

  # Append extra envs
  for kv in "$@"; do
    key=${kv%%=*}
    val=${kv#*=}
    env_json="$env_json, {\"name\":\"$key\",\"value\":\"$val\"}"
  done

  kubectl run "$pod_name" --rm -it \
    --namespace="$namespace" \
    --image="$image" \
    --restart=Never \
    --image-pull-policy=IfNotPresent \
    --overrides="
    {
        \"spec\": {
            \"containers\": [{
            \"name\": \"$pod_name\",
            \"image\": \"$image\",
            \"imagePullPolicy\": \"IfNotPresent\",
            \"command\": [\"bash\", \"-c\", \".odin/pre-deploy.sh\"],
            \"env\": [ $env_json ],
            \"volumeMounts\": [{
                \"mountPath\": \"/root/.m2/\",
                \"name\": \"host-data\"
            }]
            }],
            \"volumes\": [{
            \"name\": \"host-data\",
            \"hostPath\": {
                \"path\": \"/mnt/shared-data/.m2/\",
                \"type\": \"Directory\"
            }
            }]
        }
    }"
}

yq eval '.applications[] | @json' "$YAML_FILE" | while read -r app; do
  application=$(echo "$app" | yq eval '.application' - -p json)
  image=$(echo "$app" | yq eval '.image' - -p json)

  # Build extra envs string from YAML
  extra_envs=$(echo "$app" | yq eval '.env[] | .name + "=" + .value' - -p json | tr '\n' ' ')

  echo ">>> Running test pod for $application"
  # Use eval to properly expand the env vars as separate arguments
  eval "set -- $extra_envs"
  run_pre_deploy_pod "darwin" "$image" "$application" "$@"
done


# SERVICE_NAME="darwin-ofs-v2"
# kubectl run ${SERVICE_NAME}-pre-deploy --rm -it \
#   --image=darwin-ofs-v2:latest \
#   --image-pull-policy=IfNotPresent \
#   --restart=Never \
#   --env="APP_DIR=${APP_DIR}" \
#   --env="ENV=${ENV}" \
#   --env="VPC_SUFFIX=${VPC_SUFFIX}" \
#   --env="TEAM_SUFFIX=${TEAM_SUFFIX}" \
#   --env="SERVICE_NAME=${SERVICE_NAME}" \
#   --env="DEPLOYMENT_TYPE=${DEPLOYMENT_TYPE}" \
#   --command -- bash -c ".odin/pre-deploy.sh"

# kubectl run test-pod --rm -it \
#   --image=darwin-ofs-v2:latest \
#   --restart=Never \
#   --image-pull-policy=IfNotPresent \
#   --env="APP_DIR=/app" \
#   --env="ENV=local" \
#   --env="VPC_SUFFIX=" \
#   --env="TEAM_SUFFIX=" \
#   --env="SERVICE_NAME=darwin-ofs-v2" \
#   --env="DEPLOYMENT_TYPE=container" \
#   --command -- bash -c ".odin/pre-deploy.sh" 

# kubectl run test-pod --rm -it \
#   --namespace=darwin \
#   --image=darwin-ofs-v2-admin:latest \
#   --restart=Never \
#   --image-pull-policy=IfNotPresent \
#   --overrides='
# {
#   "spec": {
#     "containers": [{
#       "name": "test-pod",
#       "image": "darwin-ofs-v2-admin:latest",
#       "imagePullPolicy": "IfNotPresent",
#       "command": ["bash", "-c", ".odin/pre-deploy.sh"],
#       "env": [
#         {"name":"APP_DIR","value":"/app"},
#         {"name":"ENV","value":"darwin-local"},
#         {"name":"VPC_SUFFIX","value":"-darwin-local"},
#         {"name":"TEAM_SUFFIX","value":"-darwin-local"},
#         {"name":"SERVICE_NAME","value":"darwin-ofs-v2"},
#         {"name":"DEPLOYMENT_TYPE","value":"container"},
#         {"name":"DARWIN_MYSQL_HOST","value":"darwin-mysql"},
#         {"name":"DARWIN_CASSANDRA_HOST","value":"darwin-cassandra"},
#         {"name":"DARWIN_MYSQL_USERNAME","value":"root"},
#         {"name":"DARWIN_MYSQL_PASSWORD","value":"password"},
#         {"name":"VAULT_SERVICE_MYSQL_PASSWORD","value":"username"},
#         {"name":"VAULT_SERVICE_MYSQL_USERNAME","value":"password"}
#       ],
#       "volumeMounts": [{
#         "mountPath": "/root/.m2/",
#         "name": "host-data"
#       }]
#     }],
#     "volumes": [{
#       "name": "host-data",
#       "hostPath": {
#         "path": "/mnt/shared-data/.m2/", 
#         "type": "Directory"
#       }
#     }]
#   }
# }'

