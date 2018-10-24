#!/bin/bash
set -o nounset
set -e

# Install all of the dependencies of the cloud functions
pushd functions
npm install
popd

# Deploy functions to your Firebase project
firebase --project="$PROJECT_ID" deploy --only functions
