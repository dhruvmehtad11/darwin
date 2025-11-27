#!/usr/bin/env bash

fsx_root=$1
folder=$2

cd /var/www/"$fsx_root"

rm -rf "$folder"