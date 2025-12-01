BINARY_NAME=darwin-cluster-manager
OS=linux

build:
	GOARCH=amd64 GOOS=${OS} go build -o ${BINARY_NAME}-${OS} main.go

run:
	./${BINARY_NAME}-${OS}

build_and_run: build run

clean:
	go clean
	rm ${BINARY_NAME}-darwin
	rm ${BINARY_NAME}-linux
	rm ${BINARY_NAME}-windows
