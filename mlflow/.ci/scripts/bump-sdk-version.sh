#!/usr/bin/env bash
set -e

commit_message="chore: "

pushd sdk
bump2version patch --no-commit --allow-dirty
popd
git add sdk/setup.py
git add sdk/.bumpversion.cfg

commit_message=$commit_message"release sdk concert version"

git config --global user.email "release-bot@dream11.com"
git config --global user.name "release-bot"
git commit -m "$commit_message"
git push origin HEAD:main
