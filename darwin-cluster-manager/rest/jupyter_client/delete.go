package jupyter_client

import (
	"compute/cluster_manager/dto/jupyter"
	jupyter_client_service "compute/cluster_manager/services/jupyterClient"
	"github.com/gin-gonic/gin"
	"net/http"
)

func Delete(c *gin.Context) {
	var body jupyter.DeleteParams
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var jupyterClient jupyter_client_service.JupyterService

	deleteResponse := jupyterClient.DeleteJupyterClient(body)

	if deleteResponse.Err != nil {
		c.JSON(http.StatusInternalServerError, deleteResponse)
	} else {
		c.JSON(http.StatusOK, deleteResponse)
	}
}
