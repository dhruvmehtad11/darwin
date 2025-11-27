package resource_instance

type ResourceInstanceResponse struct {
	Status  string      `json:"status"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

type PodsData struct {
	Name    string `json:"name"`
	Status  string `json:"status"`
	Message string `json:"message"`
}

type ResourceStatus struct {
	Pods []PodsData `json:"pods"`
}
