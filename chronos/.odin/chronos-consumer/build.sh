SERVICE_NAME="chronos-consumer"

mkdir -p ./target/${SERVICE_NAME}/
cp -Rf ./src ./target/${SERVICE_NAME}/
cp -Rf ./scripts ./target/${SERVICE_NAME}/
cp -Rf ./resources ./target/${SERVICE_NAME}/
cp ./requirements.txt ./target/${SERVICE_NAME}/
