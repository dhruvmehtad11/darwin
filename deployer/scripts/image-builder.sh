#!/bin/sh
set -e

<<<<<<< HEAD
while getopts a:p:t:e: flag
=======
while getopts a:p:t: flag
>>>>>>> 7c7f5818ccb068f2a76688d69ccb0af87bf0b924
do
    case "${flag}" in
        a) application=${OPTARG};;
        p) path=${OPTARG};;
        t) base_path=${OPTARG};;
<<<<<<< HEAD
        e) base_image=${OPTARG};;
=======
>>>>>>> 7c7f5818ccb068f2a76688d69ccb0af87bf0b924
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
<<<<<<< HEAD
echo "base_image: $base_image";
=======
>>>>>>> 7c7f5818ccb068f2a76688d69ccb0af87bf0b924

rm -rf $path/target
mkdir -p -m 755 $path/target/$application/.odinst
bash .odin/$application/build.sh
cd $path;
cp -r ../.odin/$application/. target/$application/.odin

chmod 755 target/$application/.odin
<<<<<<< HEAD
cd $cur_dir

sudo docker build \
  --build-arg BASE_IMAGE=$base_image \
  --build-arg APP_NAME=$application \
  --build-arg APP_BASE_DIR=feature-store \
  --build-arg APP_DIR=$path \
  -t $application:latest \
  -f deployer/images/Dockerfile .
=======
cd target

cp -r $application/. /app/

if [[ -f "/app/.odin/setup.sh" ]]; then
    bash /app/.odin/setup.sh
fi

cd $cur_dir
>>>>>>> 7c7f5818ccb068f2a76688d69ccb0af87bf0b924
