#!/usr/bin/env bash

user=$1
project_name=$2
codespace_name=$3
cloned_from=$4

cd ~

function createCodespace() {
    if [ -d "$1" ]; then
        echo "Codespace Exists"
    else
        mkdir "$1"
    fi
}

function createProject() {
    if [ -d "$1" ]; then
      echo "Project exists"
    else
      mkdir "$1"
    fi
}

function syncCodespaceFromS3() {
  aws s3 sync "$1" ./"$2"
}

if [ -d "$user" ]; then
  cd "$user"
else
  mkdir "$user"
  cd "$user"
fi

createProject "$project_name"
cd "$project_name"
createCodespace "$codespace_name"
syncCodespaceFromS3 "$cloned_from" "$codespace_name"