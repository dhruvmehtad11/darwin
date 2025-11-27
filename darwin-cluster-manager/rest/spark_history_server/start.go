package spark_history_server

import (
	"compute/cluster_manager/dto/spark_history_server"
	spark_history_server_service "compute/cluster_manager/services/spark_history_server"
	"compute/cluster_manager/utils/logger"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func Start(c *gin.Context) {
	requestId := c.GetString("requestID")
	var body spark_history_server.StartSparkHistoryServerParams

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	logger.DebugR(requestId, "Starting Spark History Server with params", zap.Any("body", body))

	var sparkHistoryServer spark_history_server_service.SparkHistoryServerService
	err := body.Validate()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Message()})
		return
	}

	startSparkHistoryServerResponse := sparkHistoryServer.StartSparkHistoryServer(body)

	if startSparkHistoryServerResponse.Status == "ERROR" {
		logger.ErrorR(requestId, "Failed to start Spark History Server", zap.Any("Error", startSparkHistoryServerResponse))
		c.JSON(http.StatusInternalServerError, startSparkHistoryServerResponse)
	} else {
		c.JSON(http.StatusOK, startSparkHistoryServerResponse)
	}
}
