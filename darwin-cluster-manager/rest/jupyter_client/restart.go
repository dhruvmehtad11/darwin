package jupyter_client

import (
	"compute/cluster_manager/dto/jupyter"
	jupyter_client_service "compute/cluster_manager/services/jupyterClient"
	"github.com/gin-gonic/gin"
	"net/http"
)

func Restart(c *gin.Context) {
	var body jupyter.Params
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var jupyterClient jupyter_client_service.JupyterService

	restartResponse := jupyterClient.RestartJupyterClient(body)

	if restartResponse.Err != nil {
		c.JSON(http.StatusInternalServerError, restartResponse)
	} else {
		c.JSON(http.StatusOK, restartResponse)
	}
}
