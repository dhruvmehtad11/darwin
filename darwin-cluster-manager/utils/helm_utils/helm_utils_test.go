package helm_utils

import (
	"compute/cluster_manager/utils/logger"
	"fmt"
	"os"
	"testing"
)

func TestUnpackHelm(t *testing.T) {
	logger.Init()
	requestId := "test-request-id"
	packedChartPath := "../../tmp/artifacts/ray-cluster-1.1.0.tgz"

	// Unpack the Helm chart.
	resp, err := UnpackHelm(requestId, packedChartPath)
	if err != nil {
		t.Errorf("Unable to unpack helm: %v", err)
	}

	// Check if the response is nil.
	if resp == nil {
		t.Errorf("UnpackHelm returned nil")
	}

	// Check if the values are nil.
	if resp.Values == nil {
		t.Errorf("UnpackHelm returned nil values")
	}

	// Print the response for debugging purposes.
	fmt.Println(resp.Values)
}

func TestPackHelmV2(t *testing.T) {
	logger.Init()
	requestId := "test-request-id"
	chartPath := "../../charts/ray-cluster"
	valsPath := "../../tmp/values/ray-cluster/values.yaml"
	destinationPath := "../../tmp/artifacts/ray-cluster"
	// Create values.yaml in the above path
	if err := os.WriteFile(valsPath, []byte("key1: value1"), 0644); err != nil {
		t.Errorf("Unable to create values.yaml: %v", err)
	}

	// Package the Helm chart.
	resp, err := PackHelmV2(requestId, chartPath, valsPath, destinationPath)
	if err != nil {
		t.Errorf("Unable to pack helm: %v", err)
	}

	// Check if the response is empty.
	if resp == "" {
		t.Errorf("PackHelmV2 returned empty response")
	}
}

// TestPackHelmV2NoDefaultOverride tests that PackHelmV2 function doesn't override the default values.yaml file.
func TestPackHelmV2NoDefaultOverride(t *testing.T) {
	logger.Init()
	requestId := "test-request-id"
	chartPath := "../../charts/ray-cluster"
	valsPath := "../../tmp/values/ray-cluster/values.yaml"
	destinationPath := "../../tmp/artifacts/ray-cluster"
	// Create values.yaml in the above path
	if err := os.WriteFile(valsPath, []byte("key1: value1"), 0644); err != nil {
		t.Errorf("Unable to create values.yaml: %v", err)
	}

	// Package the Helm chart.
	//_, err := PackHelm(chartPath, valsPath, destinationPath)
	_, err := PackHelmV2(requestId, chartPath, valsPath, destinationPath)
	if err != nil {
		t.Errorf("Unable to pack helm: %v", err)
	}

	// Check if the default values file is updated or not
	defaultValsPath := chartPath + "/values.yaml"
	defaultVals, fileErr := os.ReadFile(defaultValsPath)
	if fileErr != nil {
		t.Errorf("Unable to read default values.yaml: %v", err)
	}
	if string(defaultVals) == "key1: value1" {
		t.Errorf("Default values.yaml file is updated")
	}
}
