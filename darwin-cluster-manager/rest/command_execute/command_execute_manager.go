package command_execute

import (
	dto "compute/cluster_manager/dto/command_execute"
	"compute/cluster_manager/utils/logger"
	"compute/cluster_manager/utils/rest_errors"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"net/http"
)

func Execute(c *gin.Context) {
	requestId := c.GetString("requestID")

	var body dto.CommandExecute

	// Unmarshal the request body into the body struct
	if err := c.ShouldBindJSON(&body); err != nil {
		restErr := rest_errors.NewBadRequestError("Invalid request body", err)
		c.JSON(restErr.Status(), restErr)
		logger.ErrorR(requestId, "Failed to unmarshal request body", zap.Any("Error", err))
		return
	}

	logger.InfoR(requestId, "Executing command on resource with params", zap.Any("body", body))
	service, restErr := ValidateCommandExecuteDTO(body)
	if restErr != nil {
		c.JSON(restErr.Status(), restErr)
		logger.ErrorR(requestId, "Failed to validate request", zap.Any("Error", restErr))
		return
	}

	err := service.CommandExecute(body, requestId)
	if err != nil {
		c.JSON(err.Status(), err)
		logger.ErrorR(requestId, "Failed to execute command on resource", zap.Any("Error", err))
		c.JSON(http.StatusInternalServerError, err)
	}

	c.JSON(http.StatusOK, gin.H{"status": "SUCCESS", "message": "Command executed successfully"})
}
