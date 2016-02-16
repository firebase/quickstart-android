Firebase Storage Quickstart
==============================

Introduction
------------

- [Read more about Firebase Storage](https://developers.google.com/firebase/)

Getting Started
---------------

- [Add Firebase to your Android Project](https://developers.google.com/firebase/docs/android/setup).
- Follow the [quickstart guide](https://developers.google.com/firebase/docs/) to set up your project.
- Go to the [Google Developers Console](https://console.developers.google.com/project) and navigate to your project:
    - From the left "hamburger" menu navigate to the **API Manager** tab.
    - Click on the **Credentials** item in the left column.
    - Under the heading **OAuth 2.0 Client IDs** you should see at least one entry called
      **Web client**.  Copy the value of that entry (it should be a string ending with
      `.apps.googleusercontent.com`). Open the file `app/src/main/res/values/ids.xml`
      and replace the value of the `server_client_id` string with the value you just copied.
    - Go back to your project in the Developers Console in your browser. Click the "hamburger"
      meny and then click **Home**. On the Home page you should see the ID of your project.
      It will also be in your current browser URL as the `?project=YOUR_PROJECT_ID` parameter.
      Copy this value and replace the value of the `google_project_id` string in the same
      `app/src/res/values/ids.xml` you edited earlier.
- Run the Android application on your Android device or emulator.

Support
-------

https://developers.google.com/firebase/support/

License
-------

Copyright 2015 Google, Inc.

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

