package main

import (
	"compute/cluster_manager/app"
	"compute/cluster_manager/constants"
	"compute/cluster_manager/utils/logger"
	"compute/cluster_manager/utils/tracer_utils"
	"context"
	"fmt"
	"os"
)

func main() {
	ENV := os.Getenv("ENV")
	if ENV == "prod" || ENV == "uat" || ENV == "stag" {
		constants.ENV = ENV
	} else {
		constants.ENV = "stag"
	}
	logger.Init()

	cleanup := tracer_utils.InitTracer()
	defer cleanup(context.Background())

	fmt.Println("ENV:", constants.ENV)
	app.StartApplication()
	fmt.Print("Application Started")
	logger.Info("Application Started")
}
