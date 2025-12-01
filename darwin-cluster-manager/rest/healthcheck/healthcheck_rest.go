package healthcheck

import (
	"compute/cluster_manager/utils/logger"
	"github.com/gin-gonic/gin"
	"net/http"
)

func Healthcheck(c *gin.Context) {
	requestId := c.GetString("requestID")
	c.String(http.StatusOK, "Active")
	logger.InfoR(requestId, "Healthcheck Active")
}
