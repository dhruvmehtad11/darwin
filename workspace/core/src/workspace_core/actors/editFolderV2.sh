#!/usr/bin/env bash

fsx_root=$1
old_folder=$2
new_folder=$3

cd /var/www/"$fsx_root"

mv "$old_folder" "$new_folder"