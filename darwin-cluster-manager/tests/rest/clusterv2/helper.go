package compute

import (
	"bytes"
	"k8s.io/apimachinery/pkg/util/json"
	"testing"
)

func ParseResponse(t *testing.T, resp *bytes.Buffer) map[string]interface{} {
	var Response map[string]interface{}
	if err := json.Unmarshal(resp.Bytes(), &Response); err != nil {
		t.Fatal(err)
	}

	return Response
}
