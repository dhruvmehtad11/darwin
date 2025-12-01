#!/usr/bin/env bash

fsx_root=$1
user=$2
github_link=$3
project_name=$4
codespace_name=$5

cd /var/www/"$fsx_root"

function cloneProject() {
  git clone "$1"
  mv "$2" "$3"
  mkdir "$2"
  mv "$3" "$2"
  cd "$2"
  sudo chmod -R 777 .
  cd "$3"
  git config core.filemode false
  git config --add safe.directory "*"
}

if [ -d "$user" ]; then
  cd "$user"
else
  mkdir "$user"
  cd "$user"
fi


cloneProject "$github_link" "$project_name" "$codespace_name"