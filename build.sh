#!/bin/bash

# Exit on error
set -e

# List of all samples
samples=( admob analytics app-indexing auth config crash database dynamiclinks invites messaging storage )

for sample in "${samples[@]}"
do
  echo "Building ${sample}"

  if [ $TRAVIS_PULL_REQUEST = false ] ; then
    # For a merged commit, build all configurations.
    cd $sample && \
      cp ../mock-google-services.json ./app/google-services.json && \
      ./gradlew clean build

    # Back to parent directory.
    cd -
  else
    # On a pull request, just build debug which is much faster and catches
    # obvious errors.
    cd $sample && \
      cp ../mock-google-services.json ./app/google-services.json && \
      ./gradlew clean :app:assembleDebug

    # Back to parent directory.
    cd -
  fi
done
