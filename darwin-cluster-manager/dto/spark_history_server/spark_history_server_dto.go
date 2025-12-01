package spark_history_server

import (
	"compute/cluster_manager/utils/rest_errors"
	"strings"
)

type FileSystemType string

const (
	S3FileSystem  FileSystemType = "s3"
	EFSFileSystem FileSystemType = "efs"
)

type StartSparkHistoryServerParams struct {
	Id             string         `json:"id"`
	Resource       string         `json:"resource"`
	FileSystem     FileSystemType `json:"filesystem"`
	EventsPath     string         `json:"events_path"`
	Ttl            int            `json:"ttl"`
	User           string         `json:"user"`
	KubeCluster    string         `json:"kube_cluster"`
	Namespace      string         `json:"namespace"`
	KubeClusterKey string         `json:"kube_cluster_key"`
}

type StopSparkHistoryServerParams struct {
	Id          string `json:"id"`
	KubeCluster string `json:"kube_cluster"`
	Namespace   string `json:"namespace"`
}

type GetSparkHistoryServerStatusParams struct {
	Id          string
	KubeCluster string
	Namespace   string
}

func (sparkHistoryServer *StartSparkHistoryServerParams) Validate() rest_errors.RestErr {
	sparkHistoryServer.Id = strings.TrimSpace(sparkHistoryServer.Id)
	if sparkHistoryServer.Id == "" {
		return rest_errors.NewBadRequestError("Id for history server is mandatory", nil)
	}
	if sparkHistoryServer.Resource == "" {
		return rest_errors.NewBadRequestError("Resource for history server is mandatory", nil)
	}
	if sparkHistoryServer.FileSystem != S3FileSystem && sparkHistoryServer.FileSystem != EFSFileSystem {
		return rest_errors.NewBadRequestError("FileSystem must be either 's3' or 'efs'", nil)
	}
	if sparkHistoryServer.EventsPath == "" {
		return rest_errors.NewBadRequestError("EventsPath for history server is mandatory", nil)
	}
	if sparkHistoryServer.Ttl < 0 {
		return rest_errors.NewBadRequestError("Ttl for history server is mandatory", nil)
	}
	if sparkHistoryServer.User == "" {
		return rest_errors.NewBadRequestError("User for history server is mandatory", nil)
	}
	return nil
}
