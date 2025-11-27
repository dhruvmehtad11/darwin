package spark_history_server

import (
	"compute/cluster_manager/dto/spark_history_server"
	spark_history_server_service "compute/cluster_manager/services/spark_history_server"
	"compute/cluster_manager/utils/logger"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func Stop(c *gin.Context) {
	requestId := c.GetString("requestID")
	var body spark_history_server.StopSparkHistoryServerParams
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	logger.DebugR(requestId, "Stopping Spark History Server with params", zap.Any("body", body))
	var sparkHistoryServer spark_history_server_service.SparkHistoryServerService
	stopSparkHistoryServerResponse := sparkHistoryServer.StopSparkHistoryServer(body)

	if stopSparkHistoryServerResponse.Status == "ERROR" {
		logger.ErrorR(requestId, "Failed to stop Spark History Server", zap.Any("Error", stopSparkHistoryServerResponse))
		c.JSON(http.StatusInternalServerError, stopSparkHistoryServerResponse)
	} else {
		c.JSON(http.StatusOK, stopSparkHistoryServerResponse)
	}
}
