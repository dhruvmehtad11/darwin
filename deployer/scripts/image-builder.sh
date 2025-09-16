#!/bin/sh
set -e

while getopts a:p:t: flag
do
    case "${flag}" in
        a) application=${OPTARG};;
        p) path=${OPTARG};;
        t) base_path=${OPTARG};;
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

rm -rf $path/target
mkdir -p -m 755 $path/target/$application/.odinst
bash .odin/$application/build.sh
cd $path;
cp -r ../.odin/$application/. target/$application/.odin

chmod 755 target/$application/.odin
cd target

cp -r $application/. /app/

if [[ -f "/app/.odin/setup.sh" ]]; then
    bash /app/.odin/setup.sh
fi

cd $cur_dir