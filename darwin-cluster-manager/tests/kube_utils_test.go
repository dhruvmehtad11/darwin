package tests

import (
	"compute/cluster_manager/constants"
	"compute/cluster_manager/utils/kube_utils"
	"compute/cluster_manager/utils/logger"
	"fmt"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetAllReleases(t *testing.T) {
	logger.Init()
	namespace := "ray"
	kubeConfigPath := "../configs/kind"
	selectors := constants.RayHeadNodeSelector
	releaseNameLabel := constants.RayNodeReleaseNameLabel
	resp, err := kube_utils.GetAllReleases(namespace, kubeConfigPath, selectors, releaseNameLabel, "")
	if err != nil {
		t.Errorf("GetAllReleases() failed: %v", err)
	}

	assert.IsType(t, []kube_utils.PodReleaseNameDto{}, resp)
}

func TestExecuteCommandOnMultiplePodsContainer(t *testing.T) {
	// Run Command on head pod of a RayCluster
	logger.Init()
	requestId := "test-request-id"

	namespace := "ray"
	kubeConfigPath := "../configs/kind"
	clusterName := "id-x8b6gkyezp4ha4pa"
	containerName := "ray-head"
	command := "nohup /tmp/remote-command/run-remote-command.sh c429608451364cfc8f9d54db52d322ca >> remote-command.log 2>&1 &"

	labelSelector := "rayCluster=" + clusterName + "-kuberay, ray.io/node-type=head"
	resp, err := kube_utils.ExecuteCommandOnMultiplePodsContainer(requestId, kubeConfigPath, namespace, labelSelector, containerName, command)
	if err != nil {
		t.Errorf("ExecuteCommand() failed: %v", err)
	}
	fmt.Println(resp)
	assert.IsType(t, nil, err)
}
