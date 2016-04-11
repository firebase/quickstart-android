#!/bin/bash
# 
# Copyright 2016 Google Inc. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 


#Count number of .bak fis to see if the script was already run.
function countBakFiles() {
    echo $(find . -name '*.bak' |  wc -l)
}

# Prints all Backup files. Indented.
function printBak() {
    find . -name '*.bak' | sed "s/^\.\//  /"
}

function printHelp() {
    echo "Sets the App DB URL and App ID in all the right places."
    echo ""
    if [ "$1" -ne 0 ]; then
        echo "This only works once as it needs templates inside the files it modifies."
        echo "To run it again restore all the .bak files listed below first:"
        printBak
        echo ""
        echo "Or run again as: ./setup.sh --restore"
    else
        echo "This only works once as it needs templates inside the files it modifies."
        echo "To run it again restore all the .bak files that will be created."
    fi
    echo ""
    echo "Usage: ./setup.sh <DATABASE_URL>"
    echo "e.g: ./setup.sh https://database-quickstart-12345.firebaseio.com/"
}

count=$(countBakFiles)

echo ""
# If no arguments were provided print the message.
if [ "$#" -ne 1 ]; then
    printHelp
    echo ""
    exit
fi

# Prints message if backup files are available and users tried to do a setup.
if [ "$count" -ne 0 ] && [ "$1" != "--restore" ]; then
    echo "This script can only be ran once as it needs templates inside the files it modifies."
    echo "To run it again restore all the .bak files listed below:"
    printBak
    echo ""
    echo "Or run again as: ./setup.sh --restore"
    echo ""
    exit
fi

# Restoring backup files.
if [ "$count" -ne 0 ] && [ "$1" = "--restore" ]; then
    echo "Restoring" $count "backup files..."
    bakFiles=$(find . -name '*.bak')
    for bakFile in $bakFiles; do
      mv $bakFile ${bakFile//\.bak/}
    done
    echo "Done!"
    echo ""
    exit
fi

if [ "$count" -eq 0 ]; then
    dburl=$1
    dburl="${dburl//\//\\/}" # Regex escape slashes
    dburl="${dburl//\./\\.}" # Regex escape dots

    appid=$(echo $1 | sed 's/https:\/\/\([^\.]*\)\..*/\1/g')

    if [ "$(uname)" == "Darwin" ]; then
        sed -i ".bak" 's/<DATABASE_URL>/'$dburl'/g' functions/index.js
        sed -i ".bak" 's/<APP_ID>/'$appid'/g' firebase.json
        echo "Done!"
        echo ""
        echo "Modified files backups:"
        printBak

    elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
        sed -i".bak" 's/<DATABASE_URL>/'$dburl'/g' functions/index.js
        sed -i".bak" 's/<APP_ID>/'$appid'/g' firebase.json
        echo "Done!"
        echo ""
        echo "Modified files backups:"
        printBak
    elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
        echo "Windows not supported"
    else
        echo "Your platform is not supported"
    fi
    echo ""
    exit
fi
printHelp
echo ""
