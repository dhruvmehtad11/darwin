package app

import (
	"compute/cluster_manager/constants"
	"compute/cluster_manager/utils/logger"
	"github.com/gin-gonic/gin"
	"go.opentelemetry.io/contrib/instrumentation/github.com/gin-gonic/gin/otelgin"
	"os"
)

var (
	PORT        = constants.PORT
	serviceName = os.Getenv("SERVICE_NAME")
)

var (
	router = gin.New()
)

func StartApplication() {
	router.Use(logger.RequestLoggerMiddleware())
	router.Use(gin.Recovery())
	router.Use(otelgin.Middleware(serviceName))

	mapAPIs()

	err := router.Run(":" + PORT)
	if err != nil {
		return
	}
}
