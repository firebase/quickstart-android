#!/bin/bash

samples=( admob analytics app-indexing auth config crash database dynamiclinks invites messaging storage )

for sample in "${samples[@]}"
do
  echo "Building ${sample}"
  cd $sample && \
    cp ../mock-google-services.json ./app/google-services.json ; \
    ./gradlew clean build ; \
    rm app/google-services.json ; \
    cd -
done
