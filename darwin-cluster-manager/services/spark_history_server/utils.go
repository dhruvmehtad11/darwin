package spark_history_server_service

import (
	"compute/cluster_manager/dto/spark_history_server"
	"compute/cluster_manager/utils/rest_errors"
	"fmt"
	"gopkg.in/yaml.v2"
	"helm.sh/helm/v3/pkg/chart"
	"helm.sh/helm/v3/pkg/chart/loader"
	"io/ioutil"
	"math/rand"
	"os"
	"path/filepath"
	"strconv"
)

func MakeDir(path string) error {
	fileError := os.MkdirAll(path, 0755)
	if fileError != nil {
		return fileError
	}

	return nil
}

func MakeYaml(params spark_history_server.StartSparkHistoryServerParams, env string, chart *chart.Chart) (string, rest_errors.RestErr) {
	values := map[string]interface{}{
		"nameOverride": params.Id,
	}

	if params.FileSystem == "s3" {
		values["s3"] = map[string]interface{}{
			"enableS3":     true,
			"enableIAM":    true,
			"logDirectory": params.EventsPath,
		}
	} else {
		// TODO: This 20 number is constant, should be received from compute service as a parameter
		randomInt := rand.Intn(20)
		values["pvc"] = map[string]interface{}{
			"enablePVC":         true,
			"existingClaimName": "fsx-claim-" + strconv.Itoa(randomInt),
			"eventsDir":         params.EventsPath,
		}
	}
	values["environment"] = map[string]interface{}{
		"SPARK_HISTORY_OPTS": fmt.Sprintf("-Dspark.ui.proxyRedirectUri=/ -Dspark.ui.proxyBase=/%s/%s", params.KubeClusterKey, params.Id),
		"RESOURCE":           params.Resource,
		"TTL":                params.Ttl,
		"USER":               params.User,
		"ENV":                env,
	}
	values["resource"] = params.Resource
	values["ttl"] = params.Ttl
	values["user"] = params.User
	values["env"] = env
	values["kubeClusterKey"] = params.KubeClusterKey

	chartValues := chart.Values

	for k, v := range values {
		chartValues[k] = v
	}

	mergedYAML, yamlError := yaml.Marshal(chartValues)
	if yamlError != nil {
		return "", rest_errors.NewInternalServerError("Unable to marshal json into yaml", yamlError)
	}

	filePath := fmt.Sprintf("%s%s.yaml", LocalSparkHistoryServerArtifactValuesPath, params.Id)
	fileError := MakeDir(LocalSparkHistoryServerArtifactValuesPath)
	if fileError != nil {
		return "", rest_errors.NewInternalServerError("Unable to create local destination file", fileError)
	}

	fileError = ioutil.WriteFile(filePath, mergedYAML, 0644)
	if fileError != nil {
		return "", rest_errors.NewInternalServerError("Unable to write yaml to file ", fileError)
	}

	return filePath, nil
}

func MakeHelmChart(params spark_history_server.StartSparkHistoryServerParams, env string) (string, error) {
	chartPath := SparkHistoryServerChartPath
	localChart, chartLoadError := loader.Load(chartPath)
	if chartLoadError != nil {
		return "", chartLoadError
	}

	filePath, err := MakeYaml(params, env, localChart)
	if err != nil {
		return "", err
	}
	return filePath, err
}

func DeleteFile(filePath string) (string, rest_errors.RestErr) {
	err := os.RemoveAll(filePath)
	if err != nil {
		return "", rest_errors.NewInternalServerError("Unable to delete local yaml file", err)
	}
	return "Deleted file successfully", nil
}

func DeleteSparkHistoryServerArtifacts(id string) SparkHistoryServerResponse {
	artifactPath, fileErr := filepath.Abs(LocalArtifactPath + id)
	if fileErr != nil {
		return SparkHistoryServerResponse{Status: "ERROR", Message: "Internal Server Error", Data: rest_errors.NewInternalServerError("Finding file error", fileErr)}
	}

	valuesFilePath, fileErr := filepath.Abs(LocalSparkHistoryServerArtifactValuesPath + id + ".yaml")
	if fileErr != nil {
		return SparkHistoryServerResponse{Status: "ERROR", Message: "Internal Server Error", Data: rest_errors.NewInternalServerError("Finding values file error", fileErr)}
	}

	_, err := DeleteFile(artifactPath)
	if err != nil {
		return SparkHistoryServerResponse{Status: "ERROR", Message: "Internal Server Error", Data: rest_errors.NewInternalServerError("Unable to delete artifact", err)}
	}

	valDeleteError := os.Remove(valuesFilePath)
	if valDeleteError != nil {
		return SparkHistoryServerResponse{Status: "ERROR", Message: "Internal Server Error", Data: rest_errors.NewInternalServerError("Unable to delete values", valDeleteError)}
	}
	return SparkHistoryServerResponse{Status: "SUCCESS", Message: "Delete local files successful", Data: nil}
}
