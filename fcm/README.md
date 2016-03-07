Firebase Cloud Messaging Quickstart
==============================

The Firebase Cloud Messaging Android Quickstart app demonstrates registering
an Android app for Cloud Messaging and handling the receipt of a message.
InstanceID allows easy registration while GcmReceiver and

Introduction
------------

- [Read more about Firebase Cloud Messaging](https://developers.google.com/firebase/)

Getting Started
---------------

- [Add Firebase to your Android Project](https://developers.google.com/firebase/docs/android/setup).
- Follow the [quickstart guide](https://developers.google.com/cloud-messaging/) to set up your project.
- Run the sample on Android device or emulator.

Sending Notifications
---------------------

Use Firebase console to send FCM messages to device or emulator.
- From Firebase console Notification section click New Message button.
- Enter the text of your message in the Message Text field.
- Ensure target is set to All.
  - You can also set target to Topic and choose the global topic.
- Click Send Message button.
- If your application is in the foreground you should see the incoming
  message printed in the logs. Otherwise a system notification should be
  displayed, and when tapped should return to the quickstart.

Screenshots
-----------
<img src="app/src/main/gcm-sample.png" height="534" width="300"/>

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
