#!/usr/bin/env bash

fsx_root=$1
user=$2
project_name=$3
codespace_name=$4
cloned_from=$5

cd /var/www/"$fsx_root"

function createCodespace() {
    ls
    if [ -d "$1" ]; then
        echo "Codespace Exists"
    else
        mkdir "$1"
        sudo chmod 777 "$1"
    fi
}

function createProject() {
    ls
    if [ -d "$1" ]; then
      echo "Project exists"
    else
      mkdir "$1"
    fi
}

function copyCodespace() {
  ls
  cp -a /var/www/"$1"/. .
}

if [ -d "$user" ]; then
  echo "cding into user"
  cd "$user"
else
  mkdir "$user"
  cd "$user"
fi

createProject "$project_name"
cd "$project_name"

createCodespace "$codespace_name"
cd "$codespace_name"


if [ "$cloned_from" != "None" ]; then
  copyCodespace "$cloned_from"
fi