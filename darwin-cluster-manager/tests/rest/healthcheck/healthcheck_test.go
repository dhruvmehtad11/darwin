package tests

import (
	"compute/cluster_manager/rest/healthcheck"
	"github.com/gin-gonic/gin"
	"gotest.tools/assert"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHealthcheck(t *testing.T) {
	// Create a Gin test context
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	// Call the Healthcheck function
	healthcheck.Healthcheck(c)

	// Assert expected response
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, "Active", w.Body.String())
}
