package serve

import (
	"compute/cluster_manager/utils/rest_errors"
	"strings"
)

type Serve struct {
	ValuesFilepath    string
	ServeName         string
	HelmChartName     string
	ServeHelmArtifact string
	ArtifactS3Url     string
	Namespace         string
	Framework         string
}

type Serves []Serve

func (serve *Serve) Validate() rest_errors.RestErr {
	serve.ServeName = strings.TrimSpace(serve.ServeName)
	if serve.ServeName == "" {
		return rest_errors.NewBadRequestError("serve name is mandatory", nil)
	}
	if strings.Contains(serve.ServeName, "_") {
		return rest_errors.NewBadRequestError("'_' is not allowed in cluster name", nil)
	}
	serve.ServeHelmArtifact = strings.TrimSpace(serve.ServeHelmArtifact)
	if serve.ServeHelmArtifact == "" {
		return rest_errors.NewBadRequestError("serve artifact name is mandatory", nil)
	}
	return nil
}
