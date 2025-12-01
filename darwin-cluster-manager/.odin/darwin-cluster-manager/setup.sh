echo "----- Download Go modules -----"
go mod tidy
go mod download

echo "----- Build binary -----"
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o cluster_manager main.go

echo "-----Printing files in the directory-----"
ls -la

echo "-----Printing env variables-----"
printenv
