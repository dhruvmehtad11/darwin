package kube_utils

import (
	"bytes"
	"compute/cluster_manager/utils/logger"
	"compute/cluster_manager/utils/rest_errors"
	"context"
	"fmt"
	"go.uber.org/zap"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/tools/remotecommand"
	"sync"
)

type ContainerLogsDto struct {
	ContainerName string `json:"container_name"`
	Stdout        string `json:"stdout"`
	Stderr        string `json:"stderr"`
}

type PodLogsDto struct {
	PodName        string             `json:"pod_name"`
	ContainersLogs []ContainerLogsDto `json:"containers_logs"`
}

// ExecuteCommandOnMultiplePodsContainer executes a command on multiple pods
func ExecuteCommandOnMultiplePodsContainer(requestId string, kubeConfigPath string, namespace string, labelSelector string, containerName string, command string) ([]PodLogsDto, rest_errors.RestErr) {
	var podsLogs []PodLogsDto
	var wg sync.WaitGroup
	var mu sync.Mutex

	// Build Kubernetes config
	config, err := clientcmd.BuildConfigFromFlags("", kubeConfigPath)
	if err != nil {
		return podsLogs, rest_errors.NewInternalServerError(fmt.Sprintf("Failed to build config from the config path %s", kubeConfigPath), err)
	}
	logger.DebugR(requestId, "Kube config built", logger.Field("kube_config_path", kubeConfigPath))

	// Create Kubernetes client
	clientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		return podsLogs, rest_errors.NewInternalServerError("Failed to initiate k8s client", err)
	}
	logger.DebugR(requestId, "Kube client initiated")

	// List pods based on label selector
	pods, err := clientSet.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{
		LabelSelector: labelSelector,
	})
	if err != nil {
		return podsLogs, rest_errors.NewInternalServerError(fmt.Sprintf("Failed to list pods with labels: %s", labelSelector), err)
	}
	logger.DebugR(requestId, fmt.Sprintf("Found %d pods with label selector: %s", len(pods.Items), labelSelector))

	// Iterate over each pod and execute the command in parallel
	for _, pod := range pods.Items {
		wg.Add(1)
		go func(podName string) {
			defer wg.Done()
			logger.DebugR(requestId, fmt.Sprintf("Executing command on pod: %s", podName))
			logs, err := ExecuteCommandOnContainer(config, clientSet, namespace, podName, containerName, command, requestId)
			if err != nil {
				logger.ErrorR(requestId, fmt.Sprintf("Failed to execute command on pod %s", podName), zap.Error(err))
				return
			}
			logger.DebugR(requestId, fmt.Sprintf("Command executed successfully on pod: %s", podName))

			// Append the result to podsLogs slice safely
			mu.Lock()
			podsLogs = append(podsLogs, PodLogsDto{
				PodName:        podName,
				ContainersLogs: []ContainerLogsDto{logs},
			})
			mu.Unlock()
		}(pod.Name)
	}

	// Wait for all goroutines to finish
	wg.Wait()

	return podsLogs, nil
}

func ExecuteCommandOnContainer(config *rest.Config, clientSet *kubernetes.Clientset, namespace string, podName string, containerName string, command string, requestId string) (ContainerLogsDto, rest_errors.RestErr) {
	var containerLogs ContainerLogsDto
	// Split the command string into individual arguments
	commandArgs := []string{"/bin/sh", "-c", command}

	req := clientSet.CoreV1().RESTClient().
		Post().
		Resource("pods").
		Name(podName).
		Namespace(namespace).
		SubResource("exec").
		VersionedParams(&corev1.PodExecOptions{
			Container: containerName,
			Command:   commandArgs,
			Stdin:     false,
			Stdout:    true,
			Stderr:    true,
			TTY:       false,
		}, scheme.ParameterCodec)

	executor, err := remotecommand.NewSPDYExecutor(config, "POST", req.URL())
	if err != nil {
		return containerLogs, rest_errors.NewInternalServerError("Failed to create executor", err)
	}

	// Buffers for capturing output
	var stdout, stderr bytes.Buffer

	// Stream command output
	err = executor.Stream(remotecommand.StreamOptions{
		Stdout: &stdout,
		Stderr: &stderr,
		Tty:    false,
	})
	if err != nil {
		return containerLogs, rest_errors.NewInternalServerError("Command execution failed", err)
	}
	containerLogs = ContainerLogsDto{
		ContainerName: containerName,
		Stdout:        stdout.String(),
		Stderr:        stderr.String(),
	}
	logger.DebugR(requestId, "Command executed successfully", logger.Field("stdout", containerLogs.Stdout), logger.Field("stderr", containerLogs.Stderr))
	return containerLogs, nil
}
