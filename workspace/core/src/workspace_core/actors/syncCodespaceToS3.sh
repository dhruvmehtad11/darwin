#!/usr/bin/env bash

user=$1
project=$2
codespace=$3
bucket=$4

aws s3 sync ~/"$user"/"$project"/"$codespace" "$bucket""$user"/"$project"/"$codespace" --exclude ".*/*" --exclude ".*" --delete