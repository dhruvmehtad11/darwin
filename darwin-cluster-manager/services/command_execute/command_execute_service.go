package command_execute

import (
	dto "compute/cluster_manager/dto/command_execute"
	"compute/cluster_manager/utils/rest_errors"
)

type CommandInstanceInterface interface {
	CommandExecute(request dto.CommandExecute, requestId string) rest_errors.RestErr
}
