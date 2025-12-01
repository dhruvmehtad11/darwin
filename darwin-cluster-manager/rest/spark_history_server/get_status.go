package spark_history_server

import (
	"compute/cluster_manager/dto/spark_history_server"
	spark_history_server_service "compute/cluster_manager/services/spark_history_server"
	"compute/cluster_manager/utils/logger"
	"compute/cluster_manager/utils/rest_errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func ValidateParams(params spark_history_server.GetSparkHistoryServerStatusParams) rest_errors.RestErr {
	if params.Id == "" {
		return rest_errors.NewBadRequestError("Id for history server is mandatory", nil)
	}
	if params.KubeCluster == "" {
		return rest_errors.NewBadRequestError("KubeCluster for history server is mandatory", nil)
	}
	if params.Namespace == "" {
		return rest_errors.NewBadRequestError("Namespace for history server is mandatory", nil)
	}
	return nil
}

func GetStatus(c *gin.Context) {
	requestId := c.GetString("requestID")
	id := c.Param("id")
	kubeCluster := c.Query("kube_cluster")
	namespace := c.Query("namespace")

	GetStatusParams := spark_history_server.GetSparkHistoryServerStatusParams{Id: id, KubeCluster: kubeCluster, Namespace: namespace}
	logger.DebugR(requestId, "Getting Spark History Server status with params", zap.Any("body", GetStatusParams))
	err := ValidateParams(GetStatusParams)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Message()})
		return
	}

	var sparkHistoryServer spark_history_server_service.SparkHistoryServerService

	statusResponse := sparkHistoryServer.GetSparkHistoryServer(id, kubeCluster, namespace)

	if statusResponse.Status == "ERROR" {
		logger.ErrorR(requestId, "Failed to get Spark History Server status", zap.Any("Error", statusResponse))
		c.JSON(http.StatusInternalServerError, statusResponse)
	} else {
		c.JSON(http.StatusOK, statusResponse)
	}
}
