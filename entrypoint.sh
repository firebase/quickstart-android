#!/bin/bash

# Exit on error
set -e

# unshallow since GitHub actions does a shallow clone
git fetch --unshallow
git fetch origin

# Checking the output of git remote

echo "base: $GITHUB_BASE_REF"
echo "ref: $GITHUB_REF"

echo "Running git remote"
git remote

echo "Running git branch"
git branch

current_branch=$GITHUB_BASE_REF
# Check if this is a fork
if [ -z ${GITHUB_BASE_REF+x} ]; then current_branch=$GITHUB_REF; else current_branch=origin/$GITHUB_BASE_REF; fi

# Get all the modules that were changed
while read line; do
  module_name=${line%%/*}
  if [[ ${MODULES} != *"${module_name}" ]]; then
    MODULES="${MODULES} ${module_name}"
  fi
done < <(git diff --name-only $current_branch)
changed_modules=$MODULES

# Get a list of all available gradle tasks
AVAILABLE_TASKS=$(./gradlew tasks --all)

# Check if these modules have gradle tasks
build_commands=""
for module in $changed_modules
do
  if [[ $AVAILABLE_TASKS =~ $module":app:" ]]; then
    build_commands=${build_commands}" :"${module}":app:$1 :"${module}":app:check"
  fi
done

# Build
echo "Building Pull Request with"
echo "$build_commands"
eval "./gradlew $2 ${build_commands}"
