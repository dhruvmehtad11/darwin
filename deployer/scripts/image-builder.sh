#!/bin/sh
set -e

build_args=""

while getopts a:p:t:e:r:B:d: flag
do
    case "${flag}" in
        a) application=${OPTARG};;
        p) path=${OPTARG};;
        t) base_path=${OPTARG};;
        e) base_image=${OPTARG};;
        r) registry=${OPTARG};;
        B) build_args=${OPTARG};;
        d) deployment_type=${OPTARG};;
    esac
done

if [[ -z "$application" ]]; then
    echo 'Missing option -a (application name)' >&2
    exit 1
fi

if [[ -z "$path" ]]; then
    echo 'Missing option -p (path to application directory)' >&2
    exit 1
fi

if [[ -z "$base_path" ]]; then
    echo 'Missing option -t (base_path to application directory)' >&2
    exit 1
fi

cur_dir=$(pwd)
cd $base_path

echo "application: $application";
echo "path: $path";
echo "base_path: $base_path";
echo "base_image: $base_image";
echo "registry: $registry";
echo "deployment_type: $deployment_type";
echo "dynamic build_args: $build_args";

rm -rf $path/target
mkdir -p -m 755 $path/target/$application/.odinst
echo "Building $application using build.sh"
bash -x .odin/$application/build.sh
cd $path;
echo "realpath $(realpath "$path"): $(realpath .)"
if [ "$(realpath "$path")" != "$(realpath .)" ]; then
    echo "Copying ../.odin/$application/. to target/$application/.odin"
    cp -r ../.odin/$application/. target/$application/.odin
else
    echo "Copying ./.odin/$application/. to target/$application/.odin"
    cp -r ./.odin/$application/. target/$application/.odin
fi

chmod 755 target/$application/.odin
chmod +x target/$application/.odin/start.sh
cd $cur_dir

docker build \
  --no-cache \
  --build-arg BASE_IMAGE=$base_image \
  --build-arg APP_NAME=$application \
  --build-arg APP_BASE_DIR=$base_path \
  --build-arg APP_DIR=$path \
  --build-arg DEPLOYMENT_TYPE=$deployment_type \
  --build-arg EXTRA_ENV_VARS="$build_args" \
  -t $application:latest \
  -f deployer/images/Dockerfile .
  
docker tag "$application":latest "$registry/$application":latest
docker push "$registry/$application":latest

