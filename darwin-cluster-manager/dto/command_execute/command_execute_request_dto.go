package command_execute

// CommandExecute to execute command on multiple pods
type CommandExecute struct {
	KubeCluster   string `json:"kube_cluster"`
	KubeNamespace string `json:"kube_namespace"`
	LabelSelector string `json:"label_selector"`
	ContainerName string `json:"container_name"`
	Command       string `json:"command"`
}
