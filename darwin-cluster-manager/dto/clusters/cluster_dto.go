package clusters

import (
	"compute/cluster_manager/utils/rest_errors"
	"strings"
)

type Cluster struct {
	ValuesFilepath      string
	ClusterName         string
	HelmChartName       string
	ClusterHelmArtifact string
	ArtifactS3Url       string
	Namespace           string
}

type Clusters []Cluster

func (cluster *Cluster) Validate() rest_errors.RestErr {
	cluster.ClusterName = strings.TrimSpace(cluster.ClusterName)
	if cluster.ClusterName == "" {
		return rest_errors.NewBadRequestError("cluster name is mandatory", nil)
	}
	if strings.Contains(cluster.ClusterName, "_") {
		return rest_errors.NewBadRequestError("'_' is not allowed in cluster name", nil)
	}
	cluster.ClusterHelmArtifact = strings.TrimSpace(cluster.ClusterHelmArtifact)
	if cluster.ClusterHelmArtifact == "" {
		return rest_errors.NewBadRequestError("cluster artifact name is mandatory", nil)
	}
	return nil
}
