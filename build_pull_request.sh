#!/bin/bash

# Exit on error
set -e

# unshallow since GitHub actions does a shallow clone
git fetch --unshallow
git fetch origin

echo "HEAD branch: ${GITHUB_HEAD_REF}"
echo "BASE branch: ${GITHUB_BASE_REF}"
echo "Repo: ${GITHUB_REPOSITORY}"

# Get all the modules that were changed
while read line; do
  module_name=${line%%/*}
  if [[ ${MODULES} != *"${module_name}"* ]]; then
    MODULES="${MODULES} ${module_name}"
  fi
done < <(git diff --name-only origin/$GITHUB_BASE_REF..origin/$GITHUB_HEAD_REF)
changed_modules=$MODULES

# Get a list of all available gradle tasks
AVAILABLE_TASKS=$(./gradlew tasks --all)

# Check if these modules have gradle tasks
build_commands=""
for module in $changed_modules
do
  if [[ $AVAILABLE_TASKS =~ $module":app:" ]]; then
    build_commands=${build_commands}" :"${module}":app:assembleDebug :"${module}":app:check"
  fi
done

# Build
echo "Building Pull Request with"
echo $build_commands
eval "./gradlew clean ktlint ${build_commands}"
