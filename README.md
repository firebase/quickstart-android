# Firebase Quickstarts for Android

A collection of quickstart samples demonstrating the Firebase APIs on Android. For more information, see https://firebase.google.com.

## Samples

You can open each of the following samples as an Android Studio project, and run
them on a mobile device or a virtual device (AVD). When doing so you need to
add each sample app you wish to try to a Firebase project on the [Firebase
console](https://console.firebase.google.com). You can add multiple sample apps
to the same Firebase project. There's no need to create separate projects for
each app.

To add a sample app to a Firebase project, use the `applicationId` value specified
in the `app/build.gradle` file of the app as the Android package name. Download
the generated `google-services.json` file, and copy it to the `app/` directory of
the sample you wish to run.

- [Admob](admob/README.md)
- [Firebase AI Logic](firebase-ai/README.md)
- [Analytics](analytics/README.md)
- [App Distribution](appdistribution/README.md)
- [Auth](auth/README.md)
- [Remote Config](config/README.md)
- [Crashlytics](crash/README.md)
- [Realtime Database](database/README.md)
- [Data Connect](dataconnect/README.md)
- [Firestore](firestore/README.md)
- [Cloud Functions for Firebase](functions/README.md)
- [In-App Messaging](inappmessaging/README.md)
- [Cloud Messaging](messaging/README.md)
- [Performance Monitoring](perf/README.md)
- [Cloud Storage for Firebase](storage/README.md)

## How to make contributions?
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md)

[![Actions Status][gh-actions-badge]][gh-actions]
[![SAM Score][sam-score-badge]][sam-score]

[gh-actions]: https://github.com/firebase/quickstart-android/actions
[gh-actions-badge]: https://github.com/firebase/quickstart-android/actions/workflows/android.yml/badge.svg?branch=master&event=push
[sam-score]: https://ossbot.computer/samscore.html
[sam-score-badge]: https://ossbot.computer/samscorebadge?org=firebase&repo=quickstart-android
