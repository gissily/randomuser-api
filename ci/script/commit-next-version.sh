#!/bin/bash

set -e

SCRIPT=$(readlink -f "${BASH_SOURCE:-$0}")
SCRIPT_DIR_PATH=$(dirname "${SCRIPT}")
CI_DIR_PATH=$(dirname "${SCRIPT_DIR_PATH}")
ROOT_PATH=$(dirname "${CI_DIR_PATH}")

# tag release
RELEASE_VERSION=$("${ROOT_PATH}"/mvnw help:evaluate -Dexpression=project.version | grep "^[^\\[]" |grep -v Download | cut -d '-' -f 1)
TAG_NAME=${RELEASE_VERSION}.release

"${SCRIPT_DIR_PATH}"/versions-set.sh "${RELEASE_VERSION}"
"${ROOT_PATH}"/mvnw flatten:clean

git add .
git commit -m "Bump Release [${RELEASE_VERSION}]"
## github dose not support skip ci by 'git push option'
## if using '[skip ci]' in commit message, the tag build will skip too
## now using 'if' condition in github action to skip ci, if message contains "Bump Release ["
git push

git tag -a "${TAG_NAME}" -m "Release Tag ${RELEASE_VERSION}"
git push origin "${TAG_NAME}"

# next version
"${SCRIPT_DIR_PATH}"/next-version.sh -"${VERSION_DIRECTION:-s}"

"${ROOT_PATH}"/mvnw flatten:clean
"${SCRIPT_DIR_PATH}"/clean-pom.sh

NEXT_DEVELOPMENT_VERSION=$(cat /tmp/NEXT_DEVELOPMENT_VERSION)

# commit
git add .
git commit -m "Next development version (${NEXT_DEVELOPMENT_VERSION})"
git push