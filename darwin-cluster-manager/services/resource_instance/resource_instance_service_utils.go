package resource_instance

import (
	"compute/cluster_manager/utils/logger"
	"compute/cluster_manager/utils/rest_errors"
	"fmt"
	"go.uber.org/zap"
	"gopkg.in/yaml.v2"
	"helm.sh/helm/v3/pkg/chart"
	"helm.sh/helm/v3/pkg/chart/loader"
	"os"
	"path/filepath"
)

func makeDir(path string) error {
	// Create the directory at the given path if it does not exist.
	if fileError := os.MkdirAll(path, 0755); fileError != nil {
		return fileError
	}
	return nil
}

func deepMerge(dst map[string]interface{}, src map[string]interface{}) map[string]interface{} {
	for key, srcValue := range src {
		if dstValue, exists := dst[key]; exists {
			// If both are maps, merge recursively
			if srcMap, ok := srcValue.(map[string]interface{}); ok {
				if dstMap, ok := dstValue.(map[string]interface{}); ok {
					dst[key] = deepMerge(dstMap, srcMap)
					continue
				}
			}
		}
		// Otherwise, overwrite the value
		dst[key] = srcValue
	}
	return dst
}

func makeYaml(requestId string, values map[string]interface{}, chart *chart.Chart, localValuesFilePath string, artifactName string) (string, rest_errors.RestErr) {
	// Merge the values into the chart values
	oldChartValues := make(map[string]interface{})
	for k, v := range chart.Values {
		oldChartValues[k] = v
	}
	chartValues := deepMerge(oldChartValues, values)
	mergedYAML, yamlError := yaml.Marshal(chartValues)
	if yamlError != nil {
		logger.ErrorR(requestId, "Failed to marshal json into yaml while making yaml", zap.Any("Error", yamlError))
		return "", rest_errors.NewInternalServerError("Unable to marshal json into yaml", yamlError)
	}

	// Write the merged values to a file at the given path.
	filePath := filepath.Join(localValuesFilePath, fmt.Sprintf("%s.yaml", artifactName))

	if fileError := makeDir(localValuesFilePath); fileError != nil {
		logger.ErrorR(requestId, "Failed to create local destination file while making yaml", zap.Any("Error", fileError))
		return "", rest_errors.NewInternalServerError("Unable to create local destination file", fileError)
	}

	if fileError := os.WriteFile(filePath, mergedYAML, 0644); fileError != nil {
		logger.ErrorR(requestId, "Failed to write yaml to file while creating yaml", zap.Any("Error", fileError))
		return "", rest_errors.NewInternalServerError("Unable to write yaml to file ", fileError)
	}

	logger.DebugR(requestId, "Successfully created yaml", zap.String("filePath", filePath))
	return filePath, nil
}

func makeChartValues(requestId string, values map[string]interface{}, chartPath string, localValuesFilePath string, artifactName string) (string, rest_errors.RestErr) {
	// Load the chart from the given path.
	localChart, chartLoadError := loader.Load(chartPath)
	if chartLoadError != nil {
		logger.ErrorR(requestId, "Failed to load chart while creating chart values", zap.Any("Error", chartLoadError))
		return "", rest_errors.NewInternalServerError("Unable to load chart", chartLoadError)
	}

	// Create a yaml file with the merged values to chart values at a given path.
	filePath, err := makeYaml(requestId, values, localChart, localValuesFilePath, artifactName)
	if err != nil {
		return "", err
	}

	return filePath, err
}

func deleteFile(requestId string, filePath string) {
	// Delete the file at the given path.
	if err := os.RemoveAll(filePath); err != nil {
		logger.ErrorR(requestId, "Failed to delete local file", zap.Any("Error", err))
	}
}
