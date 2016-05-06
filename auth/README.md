Firebase Auth Quickstart
==============================

Introduction
------------

- [Read more about Firebase Auth](https://firebase.google.com)

Getting Started
---------------

- [Add Firebase to your Android Project](https://firebase.google.com/docs/android/setup).


### Google Sign In Setup

- Go to the [Firebase Console](https://console.firebase.google.com) and navigate to your project:
  - Select the **Auth** panel and then click the **Sign In Method** tab.
  - Click **Google** and turn on the **Enable** switch, then click **Save**.
- Run the app on your device or emulator.
    - Select `GoogleSignInActivity` from the main screen.
    - Click the **Sign In** button to begin.


### Facebook Login Setup

- Go to the [Facebook Developers Site](https://developers.facebook.com) and follow all
  instructions to set up a new Android app. When asked for a package name, use
  `com.google.firebase.quickstart.usermanagement` and when asked for a main class name,
  use `com.google.firebase.quickstart.usermanagement.FacebookLoginActivity`.
- Go to the [Firebase Console](https://console.firebase.google.com) and navigate to your project:
  - Select the **Auth** panel and then click the **Sign In Method** tab.
  - Click **Facebook** and turn on the **Enable** switch, then click **Save**.
  - Enter your Facebook **App Id** and **App Secret** and click **Save**.
- Open the file `app/src/main/res/values/ids.xml` and replace the value of the `facebook_app_id` with the ID of the Facebook app you just created.
- Run the app on your device or emulator.
    - Select `FacebookLoginActivity` from the main screen.
    - Click the **Sign In** button to begin.
    - If you see text that says Facebook is disabled, make sure you are running
      either the `facebookDebug` or `facebookRelease` variants of this sample.


### Email/Password Setup

- Go to the [Firebase Console](https://console.firebase.google.com) and navigate to your project:
  - Select the **Auth** panel and then click the **Sign In Method** tab.
  - Click **Email/Password** and turn on the **Enable** switch, then click **Save**.
- Run the app on your device or emulator.
    - Select `EmailPasswordActivity` from the main screen.
    - Fill in your desired email and password and click **Create Account** to begin.


### Custom Authentication Setup

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
    - Select `CustomAuthActivity` from the main screen.
    - Run the text you copied from the **ADB Command** section of the web page in the steps above.
      This should send a token to the running app and update the Custom Token field.
    - Click **Sign In** to sign in to Firebase User Management with the generated JWT. You should
      see the User ID you entered when generating the token.


Screenshots
-----------
<img src="app/src/screen.png" height="534" width="300"/>

Support
-------

https://firebase.google.com/support/

License
-------

Copyright 2016 Google, Inc.

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

