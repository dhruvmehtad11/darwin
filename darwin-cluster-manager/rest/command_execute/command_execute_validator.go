package command_execute

import (
	"compute/cluster_manager/dto/command_execute"
	commandService "compute/cluster_manager/services/command_execute"
	"compute/cluster_manager/utils/rest_errors"
)

func ValidateCommandExecuteDTO(body command_execute.CommandExecute) (commandService.CommandInstanceInterface, rest_errors.RestErr) {
	if body.KubeCluster == "" {
		return nil, rest_errors.NewBadRequestError("kube_cluster is required", nil)
	}
	if body.KubeNamespace == "" {
		return nil, rest_errors.NewBadRequestError("kube_namespace is required", nil)
	}
	if body.LabelSelector == "" {
		return nil, rest_errors.NewBadRequestError("label_selector is required", nil)
	}
	if body.ContainerName == "" {
		return nil, rest_errors.NewBadRequestError("container_name is required", nil)
	}
	if body.Command == "" {
		return nil, rest_errors.NewBadRequestError("command is required", nil)
	}
	return &commandService.CommandExecuteService{}, nil
}
