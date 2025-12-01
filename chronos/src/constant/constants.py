HEALTHCHECK_PATH = "/healthcheck"
EVENTS_PATH = "/events"
EVENT_PATH = "/event"

RAW_EVENT_PATH = "/event/raw"

SEARCH_EVENTS_PATH = "/events/search"

RELATION_PATH = "/relation"

APP_DIR_ENVIRONMENT_VARIABLE = "APP_DIR"
ENV_ENVIRONMENT_VARIABLE = "ENV"
CLUSTER_EVENTS_DEPTH = 1

sources_for_cluster = [
    "aws",
    "compute",
    "DARWIN_SPARK",
    "k8s-darwin-platform-gke-cluster-us-east4",
    "k8s-darwin-prod-cluster",
    "k8s-darwin-prod-eks-cluster",
    "k8s-darwin-prod-eks-cluster-backup",
    "k8s-darwin-prod-eks-cluster-1",
    "k8s-darwin-stag-eks-cluster-0",
    "k8s-darwin-stag-eks-cluster-1"
]
