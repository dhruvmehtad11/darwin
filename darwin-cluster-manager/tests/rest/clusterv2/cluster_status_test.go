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

func TestClusterStatus(t *testing.T) {
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	c.Request = &http.Request{
		Method: http.MethodPut,
		URL: &url.URL{
			Path: "/compute/v2/cluster/status",
		},
		PostForm: url.Values{
			"cluster_name": []string{"id-test"},
			"namespace":    []string{"ray"},
			"kube_cluster": []string{"kind"},
		},
	}

	clusterv2.Status(c)
	assert.Equal(t, http.StatusAccepted, w.Code)

	resp := ParseResponse(t, w.Body)
	assert.Equal(t, "id-test", resp["ClusterName"])
}
