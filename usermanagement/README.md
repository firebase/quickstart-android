Firebase User Management Quickstart
==============================

Introduction
------------

- [Read more about Firebase User Management](https://developers.google.com/firebase/)

Getting Started
---------------

- [Add Firebase to your Android Project](https://developers.google.com/firebase/docs/android/setup).
- Follow the [quickstart guide](https://developers.google.com/firebase/docs/remote-config/android) to set up your project.
- Go to the [Google Developers Console](https://console.developers.google.com/project) and navigate to your project:
    - From the left "hamburger" menu navigate to the **API Manager** tab.
    - Click on the **Credentials** item in the left column.
    - Click **New credentials** and select **Service account key**. Select **New service account**,
    pick any name, and select **JSON** as the key type. Then click **Create**.
    - You should now have a new JSON file for your service account in your Downloads directory.
- Open the file `web/auth.html` in your computer's web browser.
    - Click **Choose File** and upload the JSON file you just downloaded.
    - Enter any User ID and click **Generate**.
    - Copy the text from the **ADB Command** section, you will need this.
- Run the Android application on your Android device or emulator.
    - Run the text you copied from the **ADB Command** section of the web page in the steps above.
      This should send a token to the running app and update the Custom Token field.
    - Click **Sign In** to sign in to Firebase User Management with the generated JWT. You should
      see the User ID you entered when generating the token.


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

