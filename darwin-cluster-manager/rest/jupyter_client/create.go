package jupyter_client

import (
	"compute/cluster_manager/dto/jupyter"
	jupyter_client_service "compute/cluster_manager/services/jupyterClient"
	"github.com/gin-gonic/gin"
	"net/http"
)

func Create(c *gin.Context) {
	var body jupyter.Params
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var jupyterClient jupyter_client_service.JupyterService

	createClientResponse := jupyterClient.CreateJupyterClient(body)

	if createClientResponse.Err != nil {
		c.JSON(http.StatusInternalServerError, createClientResponse)
	} else {
		c.JSON(http.StatusOK, createClientResponse)
	}
}
