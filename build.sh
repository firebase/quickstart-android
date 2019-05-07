#!/bin/bash

# Exit on error
set -e

# Work off travis
if [[ ! -z TRAVIS_PULL_REQUEST ]]; then
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
else
  echo "TRAVIS_PULL_REQUEST: unset, setting to false"
  TRAVIS_PULL_REQUEST=false
fi

# Copy mock google-services file
echo "Using mock google-services.json"
cp mock-google-services.json admob/app/google-services.json
cp mock-google-services.json analytics/app/google-services.json
cp mock-google-services.json app-indexing/app/google-services.json
cp mock-google-services.json auth/app/google-services.json
cp mock-google-services.json config/app/google-services.json
cp mock-google-services.json crash/app/google-services.json
cp mock-google-services.json database/app/google-services.json
cp mock-google-services.json dynamiclinks/app/google-services.json
cp mock-google-services.json firestore/app/google-services.json
cp mock-google-services.json functions/app/google-services.json
cp mock-google-services.json inappmessaging/app/google-services.json
cp mock-google-services.json perf/app/google-services.json
cp mock-google-services.json messaging/app/google-services.json
cp mock-google-services.json mlkit/app/google-services.json
cp mock-google-services.json mlkit-langid/app/google-services.json
cp mock-google-services.json mlkit-smartreply/app/google-services.json
cp mock-google-services.json mlkit-translate/app/google-services.json
cp mock-google-services.json storage/app/google-services.json

# Install preview deps
${ANDROID_HOME}/tools/bin/sdkmanager --channel=3 \
  "tools" "platform-tools" "build-tools;26.0.0-rc2" "platforms;android-26"

# Build
if [ $TRAVIS_PULL_REQUEST = false ] ; then
  echo "Building full project"
  # For a merged commit, build all configurations.
  ./gradlew clean ktlint build
else
  # On a pull request, just build debug which is much faster and catches
  # obvious errors.
  ./gradlew clean ktlint assembleDebug check
fi
