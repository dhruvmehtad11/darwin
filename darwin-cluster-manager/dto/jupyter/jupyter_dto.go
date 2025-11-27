package jupyter

import (
	"compute/cluster_manager/utils/rest_errors"
	"fmt"
	"strings"
)

type Params struct {
	JupyterClient  string `json:"jupyter_path"`
	ReleaseName    string `json:"release_name"`
	Namespace      string `json:"namespace"`
	KubeConfig     string `json:"kube_config"`
	FsxClaim       string `json:"fsx_claim"`
	KubeClusterKey string `json:"kube_cluster_key"`
}

type DeleteParams struct {
	ReleaseName string `json:"release_name"`
	KubeConfig  string `json:"kube_config"`
	Namespace   string `json:"namespace"`
}

func (jupyter *Params) Validate() rest_errors.RestErr {
	jupyter.ReleaseName = strings.TrimSpace(jupyter.ReleaseName)
	jupyter.JupyterClient = strings.TrimSpace(jupyter.JupyterClient)

	missingFields := ""
	if jupyter.JupyterClient == "" {
		missingFields += ",JupyterClient"
	}
	if jupyter.ReleaseName == "" {
		missingFields += ",ReleaseName"
	}
	if jupyter.Namespace == "" {
		missingFields += ",Namespace"
	}
	if jupyter.KubeConfig == "" {
		missingFields += ",KubeConfig"
	}
	if jupyter.KubeClusterKey == "" {
		missingFields += ",KubeClusterKey"
	}
	if jupyter.FsxClaim == "" {
		missingFields += ",FsxClaim"
	}

	if missingFields != "" {
		missingFields = strings.TrimPrefix(missingFields, ",")
		missingFields = strings.TrimSuffix(missingFields, ",")
		return rest_errors.NewBadRequestError(fmt.Sprintf("%s is mandatory", missingFields), nil)
	}

	return nil
}

func (jupyter *DeleteParams) Validate() rest_errors.RestErr {
	missingFields := ""
	if jupyter.ReleaseName == "" {
		missingFields += ",ReleaseName"
	}
	if jupyter.Namespace == "" {
		missingFields += ",Namespace"
	}
	if jupyter.KubeConfig == "" {
		missingFields += ",KubeConfig"
	}

	if missingFields != "" {
		missingFields = strings.TrimPrefix(missingFields, ",")
		missingFields = strings.TrimSuffix(missingFields, ",")
		return rest_errors.NewBadRequestError(fmt.Sprintf("%s is mandatory", missingFields), nil)
	}

	return nil
}
