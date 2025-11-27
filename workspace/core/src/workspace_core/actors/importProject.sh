#!/usr/bin/env bash

user=$1
github_link=$2
project_name=$3
codespace_name=$4

cd ~

function cloneProject() {
  git clone "$1"
  mv "$2" "$3"
  mkdir "$2"
  mv "$3" "$2"
}

if [ -d "$user" ]; then
  cd "$user"
else
  mkdir "$user"
  cd "$user"
fi


cloneProject "$github_link" "$project_name" "$codespace_name"