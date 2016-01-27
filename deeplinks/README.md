Firebase Analytics Quickstart
==============================

Introduction
------------

- [Read more about Firebase Analytics](https://developers.google.com/firebase/)

Getting Started
---------------

- [Add Firebase to your Android Project](https://developers.google.com/firebase/docs/android/setup).
- Follow the [quickstart guide](https://developers.google.com/firebase/docs/android/setup) to set up your project.
- Configure the sample:
  - Replace the `app_code` value in `strings.xml` with your personal app code.
  - Replace the `applicationId` in `app/build.gradle` with the package name that matches your app code.
- Run the sample on your Android device or emulator.
- Using the sample:
  - When the application is started a deep link is generated using your app code. Click **Share**
    to share this deep link to another application (like Google Keep).
  - When the application is started the app checks if it was launched from a deep link. If so,
    the link data is displayed under the **Receive** heading. Try sharing the deep link from the
    app and then using that link to re-launch the application.


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

