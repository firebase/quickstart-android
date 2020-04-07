#!/bin/bash

# Exit on error
set -e

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

