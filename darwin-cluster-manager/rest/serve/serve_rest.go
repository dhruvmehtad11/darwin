package serve

import (
	"compute/cluster_manager/constants"
	"compute/cluster_manager/dto/serve"
	"compute/cluster_manager/services"
	"compute/cluster_manager/utils/rest_errors"
	"github.com/gin-gonic/gin"
	"net/http"
)

const (
	LocalValuesFolder = constants.LocalValuesFolder
	DashboardSuffix   = constants.DashboardSuffix
	MetricsSuffix     = constants.MetricsSuffix
)

type ResponseforCreate struct {
	ServeName         string
	ServeHelmArtifact string
	ArtifactS3Url     string
}

type ResponseforStart struct {
	ServeName     string
	DashboardLink string
	MetricsLink   string
}

func Create(c *gin.Context) {
	var serveObj serve.Serve
	serveObj.ServeName, _ = c.GetPostForm("serve_name")
	serveObj.ServeHelmArtifact, _ = c.GetPostForm("artifact_name")
	serveObj.Framework, _ = c.GetPostForm("framework")
	restError := serveObj.Validate()
	if restError != nil {
		c.JSON(restError.Status(), restError)
		return
	}
	file, err := c.FormFile("file")
	if err != nil {
		restError := rest_errors.NewBadRequestError("Invalid config file", err)
		c.JSON(restError.Status(), restError)
		return
	}
	filename := LocalValuesFolder + serveObj.ServeName + "-" + file.Filename
	if err := c.SaveUploadedFile(file, filename); err != nil {
		restError := rest_errors.NewInternalServerError("failed to save the file", err)
		c.JSON(restError.Status(), restError)
		return
	}
	serveObj.ValuesFilepath = filename

	result, restError := services.ServesService.CreateServe(serveObj)
	if restError != nil {
		c.JSON(restError.Status(), restError)
		return
	}
	response := ResponseforCreate{result.ServeName, result.ServeHelmArtifact, result.ArtifactS3Url}
	c.JSON(http.StatusCreated, response)
}

func Start(c *gin.Context) {
	serveName, _ := c.GetPostForm("serve_name")
	artifactName, _ := c.GetPostForm("artifact_name")
	namespace, _ := c.GetPostForm("namespace")
	kubeCluster, _ := c.GetPostForm("kube_cluster")
	url, restError := services.ServesService.StartServe(serveName, artifactName, namespace, kubeCluster)
	if restError != nil {
		c.JSON(restError.Status(), restError)
		return
	}
	response := ResponseforStart{serveName, url + "/" + serveName + DashboardSuffix, url + "/" + serveName + MetricsSuffix}
	c.JSON(http.StatusAccepted, response)
}

func Stop(c *gin.Context) {
	serveName, _ := c.GetPostForm("serve_name")
	namespace, _ := c.GetPostForm("namespace")
	kubeCluster, _ := c.GetPostForm("kube_cluster")
	restError := services.ServesService.StopServe(serveName, namespace, kubeCluster)
	if restError != nil {
		c.JSON(restError.Status(), restError)
		return
	}
	c.JSON(http.StatusAccepted, "serve application Stopped")
}

func Status(c *gin.Context) {
	serveName, _ := c.GetPostForm("serve_name")
	namespace, _ := c.GetPostForm("namespace")
	framework, _ := c.GetPostForm("framework")
	kubeCluster, _ := c.GetPostForm("kube_cluster")
	response, restError := services.ServesService.ServeStatus(serveName, namespace, framework, kubeCluster)
	if restError != nil {
		c.JSON(restError.Status(), restError)
		return
	}
	c.JSON(http.StatusAccepted, response)
}
