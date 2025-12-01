package helm_utils

import (
	"compute/cluster_manager/utils/rest_errors"
	"fmt"
	"helm.sh/helm/v3/pkg/action"
	"helm.sh/helm/v3/pkg/kube"
	"os"
)

func GetActionConfig(kubeConfigPath string, namespace string) (*action.Configuration, rest_errors.RestErr) {
	actionConfig := new(action.Configuration)
	if err := actionConfig.Init(kube.GetConfig(kubeConfigPath, "", namespace), namespace, os.Getenv("HELM_DRIVER"), func(format string, v ...interface{}) {
		_ = fmt.Sprintf(format, v)
	}); err != nil {
		restError := rest_errors.NewInternalServerError("Error initiating the action config", err)
		return nil, restError
	}
	return actionConfig, nil
}
