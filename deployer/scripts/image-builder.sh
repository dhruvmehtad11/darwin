#!/bin/sh
set -e

while getopts a:p:t:e:r: flag
do
    case "${flag}" in
        a) application=${OPTARG};;
        p) path=${OPTARG};;
        t) base_path=${OPTARG};;
        e) base_image=${OPTARG};;
        r) registry=${OPTARG};;
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

rm -rf $path/target
mkdir -p -m 755 $path/target/$application/.odinst
bash .odin/$application/build.sh
cd $path;
cp -r ../.odin/$application/. target/$application/.odin

chmod 755 target/$application/.odin
cd $cur_dir

docker build \
  --build-arg BASE_IMAGE=$base_image \
  --build-arg APP_NAME=$application \
  --build-arg APP_BASE_DIR=feature-store \
  --build-arg APP_DIR=$path \
  -t $application:latest \
  -f deployer/images/Dockerfile .
  
docker tag "$application":latest "$registry/$application":latest
docker push "$registry/$application":latest

