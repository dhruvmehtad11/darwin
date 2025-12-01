package jupyter_client_service

import (
	"compute/cluster_manager/constants"
	"compute/cluster_manager/dto/jupyter"
	"compute/cluster_manager/utils/rest_errors"
	"fmt"
	"gopkg.in/yaml.v2"
	"helm.sh/helm/v3/pkg/chart"
	"helm.sh/helm/v3/pkg/chart/loader"
	"io/ioutil"
	"os"
	"path/filepath"
)

var (
	JupyterDefaultValues = constants.DefaultJupyterValuesConstants
)

func MakeDir(path string) error {
	fileError := os.MkdirAll(path, 0755)
	if fileError != nil {
		return fileError
	}

	return nil
}

func MakeYaml(params jupyter.Params, chart *chart.Chart) (string, rest_errors.RestErr) {
	values := map[string]interface{}{
		"fullnameOverride": params.ReleaseName,
		"kubeClusterKey":   params.KubeClusterKey,
		"podLabels": map[string]interface{}{
			"org_name":            "dream11",
			"environment_name":    ENV,
			"squad":               "ml-platform",
			"provisioned-by-user": "harsh.a",
			"domain_name":         "dream11.com",
			"cluster":             params.KubeConfig,
			"service_name":        "darwin-cluster-manager",
			"component_name":      "remote-kernel",
			"component_type":      "application",
			"resource_type":       "pod",
		},
		"volumes": []map[string]interface{}{
			{
				"name": "persistent-storage",
				"persistentVolumeClaim": map[string]interface{}{
					"claimName": params.FsxClaim,
				},
			},
		},
	}

	for k, v := range JupyterDefaultValues {
		if _, ok := values[k]; !ok {
			values[k] = v
		}
	}

	chartValues := chart.Values

	for k, v := range values {
		chartValues[k] = v
	}

	mergedYAML, yamlError := yaml.Marshal(chartValues)
	if yamlError != nil {
		return "", rest_errors.NewInternalServerError("Unable to marshal json into yaml", yamlError)
	}

	filePath := fmt.Sprintf("%s%s.yaml", LocalValuesFolder, params.ReleaseName)
	fileError := MakeDir(LocalValuesFolder)
	if fileError != nil {
		return "", rest_errors.NewInternalServerError("Unable to create local destination file", fileError)
	}

	fileError = ioutil.WriteFile(filePath, mergedYAML, 0644)
	if fileError != nil {
		return "", rest_errors.NewInternalServerError("Unable to write yaml to file ", fileError)
	}

	return filePath, nil
}

func MakeHelmChart(params jupyter.Params) (string, error) {
	chartPath := JupyterChartPath
	localChart, chartLoadError := loader.Load(chartPath)
	if chartLoadError != nil {
		return "", chartLoadError
	}

	filePath, err := MakeYaml(params, localChart)
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

func DeleteJupyterArtifacts(releaseName string) JupyterClientResponse {
	jupyterArtifactPath, fileErr := filepath.Abs(LocalArtifactPath + releaseName)

	fmt.Println("jupyterArtifactPath: ", jupyterArtifactPath)
	if fileErr != nil {
		return JupyterClientResponse{Message: "", Err: rest_errors.NewInternalServerError("Unable to delete helm chart", fileErr), JupyterLink: ""}
	}
	jupyterValuePath, fileErr := filepath.Abs(LocalJupyterArtifactValuesPath + releaseName + ".yaml")

	fmt.Println("jupyterValuePath: ", jupyterValuePath)
	if fileErr != nil {
		return JupyterClientResponse{Message: "", Err: rest_errors.NewInternalServerError("Unable to delete helm chart", fileErr), JupyterLink: ""}
	}

	_, err := DeleteFile(jupyterArtifactPath)
	if err != nil {
		return JupyterClientResponse{Message: "", Err: rest_errors.NewInternalServerError("Unable to delete helm chart", err), JupyterLink: ""}
	}

	valDeleteError := os.Remove(jupyterValuePath)
	if err != nil {
		return JupyterClientResponse{Message: "", Err: rest_errors.NewInternalServerError("Unable to delete helm chart", valDeleteError), JupyterLink: ""}
	}
	return JupyterClientResponse{Message: "Delete successful", Err: nil, JupyterLink: ""}
}
