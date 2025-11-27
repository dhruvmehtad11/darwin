package compute

import (
	"bytes"
	"compute/cluster_manager/rest/clusterv2"
	"fmt"
	"github.com/gin-gonic/gin"
	"gotest.tools/assert"
	"io"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"
)

func TestCreateCluster(t *testing.T) {
	// Create a Gin test context
	fileName := "values.yaml"
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	payload := &bytes.Buffer{}
	writer := multipart.NewWriter(payload)
	file, errFile1 := os.Open(fileName)
	defer file.Close()
	part1, errFile1 := writer.CreateFormFile("file", filepath.Base(fileName))
	_, errFile1 = io.Copy(part1, file)
	if errFile1 != nil {
		fmt.Println(errFile1)
		return
	}
	_ = writer.WriteField("cluster_name", "id-test")
	_ = writer.WriteField("artifact_name", "id-test-v1")
	err := writer.Close()
	if err != nil {
		fmt.Println(err)
		return
	}

	// Set the request to the test context
	c.Request, _ = http.NewRequest(http.MethodPost, "/compute/v2/cluster", payload)
	c.Request.Header.Set("Content-Type", writer.FormDataContentType())

	clusterv2.Create(c)

	// Assert expected responses
	assert.Equal(t, http.StatusCreated, w.Code)

	resp := ParseResponse(t, w.Body)
	assert.Equal(t, "id-test", resp["ClusterName"])
	assert.Equal(t, "id-test-v1", resp["ClusterHelmArtifact"])
}
