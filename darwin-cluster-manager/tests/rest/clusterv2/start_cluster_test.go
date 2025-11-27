package compute

import (
	"compute/cluster_manager/rest/clusterv2"
	"github.com/gin-gonic/gin"
	"gotest.tools/assert"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
)

func TestStartCluster(t *testing.T) {
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	c.Request = &http.Request{
		Method: http.MethodPut,
		URL: &url.URL{
			Path: "/compute/v2/cluster/start",
		},
		PostForm: url.Values{
			"cluster_name":  []string{"id-test"},
			"artifact_name": []string{"id-test-v1"},
			"namespace":     []string{"ray"},
			"kube_cluster":  []string{"kind"},
		},
	}

	clusterv2.Start(c)

	assert.Equal(t, http.StatusAccepted, w.Code)

	resp := ParseResponse(t, w.Body)
	assert.Equal(t, "id-test", resp["ClusterName"])
}
