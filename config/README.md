Firebase Remote Config Emulator Quickstart
==============================

The Firebase Remote Config Emulator Android quickstart app demonstrates using Remote
Config Emulator to define user-facing text in an Android app.

Introduction
------------

This is a simple example of using the Remote Config Emulator to override in-app default
values by defining emulator-side parameter values in the Firebase Emulator Suite UI console.

Getting started
---------------

1. Install custom Firebase CLI. From the root of this quickstart
   1. run `npm install -g "https://firebasestorage.googleapis.com/v0/b/jeff-storage-90953.appspot.com/o/firebase-tools-11.16.0-rc-emulator.tgz?alt=media&token=bc26160a-07fc-4b70-a92b-fa11207daa8b"`
   2. run `firebase --version`
   3. Confirm that version is `11.16.0-rc-emulator`
2. Turn on the Remote Config emulator experiment
   1. run `firebase experiments:enable rcemulator`
3. Install custom Firebase Emulator UI
   1. run `npm install "https://firebasestorage.googleapis.com/v0/b/jeff-storage-90953.appspot.com/o/firebase-remote-config-0.4.3.tgz?alt=media&token=4a5100f7-e043-4ddd-898d-7638596b8d61"`
4. Initialize Firebase demo project
   1. run `firebase init emulators`
   2. select `Don't set up a default project`
   3. choose the `Remote Config Emulator` from the list of emulators
   4. `y` to download the emulators
5. Update `firebase.json` file to identify the local template file.
   1. add the following field:
```json
"remoteconfig": {
  "template": "remoteconfig.template.json"
}
```
6. Start the emulator
   1. run `firebase emulators:start --project demo-rc-emulation`

7. Run the sample on an Android emulator.
8. Change one or more parameter values in the Firebase Emulator Suite UI (the value of
`welcome_message`). 
12. Tap **Fetch Remote Config** in the app to fetch new parameter values and see
  the resulting change in the app.

Support
-------

- [GitHub Issues](<TODO>)

License
-------

Copyright 2023 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
