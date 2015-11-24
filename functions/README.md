# Firebase Cloud Functions Quickstart - Android

This quickstart demonstrates using **Google Cloud Functions** and its interaction with a **Firebase database** through a simple **Android app**.


## Introduction

This sample adds a Cloud Function that automatically makes all messages uppercase as soon as they are inserted in a Firebase database. This quickstart can be followed on its own but the Android Firebase Database quickstart app can serve as a UI for this sample so - ideally - please go through the [Android Firebase Database quickstart](../database/) first.

Further reading:

 - [Read more about Cloud Functions](https://developers.google.com/firebase/docs/cloud-functions/setup)


## Initial setup, build tools and dependencies

### 1. Install the Firebase CLI Alpha

You need to have installed [npm](https://www.npmjs.com/) which typically comes with [Node.js](https://nodejs.org).

Download the [Firebase CLI 3.0.0 Alpha](https://developers.google.com/firebase/downloads/firebase-cli.3.0.0-alpha.latest.tar.gz) and install it using:

```
npm install -g firebase-cli.3.0.0-alpha.latest.tar.gz
```

> You might have to `sudo` the command above.

Run `firebase auth` and authenticate with your Google account.


### 2. Clone this repo

Clone this repo and enter the `android/functions` directory:

```bash
git clone sso://devrel/samples/firebase/quickstart/android
cd android/functions
```


### 3. Create a Firebase project and configure the quickstart

You should have alreday created a Firebase Project on the [Firebase Console](http://g.co/firebase) for the Android Firebase Database quickstart.

Open the Firebase Console and note your Firebase database URL and your App ID. You can find your Firebase database URL in the **Database** section. It will look like:
`https://<YOUR_APP_ID>.firebaseio.com/` Note your **App ID** and also the whole **Database URL**.

Use these values to replace `<APP_ID>` in `firebase.json` and the `<DATABASE_URL>` in `functions/config.json`.
You can also do this automatically by running:

```bash
./setup.sh <DATABASE_URL>
```

For example: `./setup.sh https://functions-quickstart-12345.firebaseio.com/`.


### 4. Enable the Google Cloud Functions APIs

Enable the Google Cloud APIs required to run Cloud Functions on your project:

 - Open [this page](https://console.developers.google.com/flows/enableapi?apiid=cloudfunctions,container,compute_component,storage_component,pubsub,logging)
 - Choose the project you created earlier and click **Continue**

Enable Billing on your project:

 - Open [this page](https://console.developers.google.com/project/_/settings)
 - Choose the project you created earlier and click **Continue**
 - Click **Enable Billing**
 - Select one of your **Billing accounts**. You may have to [create a Billing account](https://console.developers.google.com/billing/create) first.


## Deploy your Cloud Functions

Deploy your Cloud Functions using the following command:

```bash
firebase deploy
```

This deploys and activates the Cloud Function that will make all of your messages uppercase.

> The first time you call `firebase deploy` on a new project the Google Compute Engine instances and Kubernetes clusters required to run Cloud Functions will be spin-up. This may take a few minutes but things will be a lot faster on subsequent deploys.


## See the results

Once your Cloud Function is deployed add the following objects to your Firebase Database manually using the Firebase Console:

```
\functions-quickstart-12345
    \messages
        \key-123456
            text: "This is my first message!"
        \key-123457
            text: "This is my second message!"
```

Adding these objects triggers the cloud function which makes the messages uppercase:

```
\functions-quickstart-12345
    \messages
        \key-123456
            text: "THIS IS MY FIRST MESSAGE!"
        \key-123457
            text: "THIS IS MY SECOND MESSAGE!"
```

Now try to add messages using the Firebase Database Quickstart Android app: the messages will now get uppercased automatically shortly after you add them.


## Debugging

Within Cloud Functions any `console.log()` statements will be logged to Cloud Logging. You can view these logs by using [this Cloud Logging filter](https://console.developers.google.com/project/_/logs?advancedFilter=metadata.serviceName:"cloudfunctions.googleapis.com").

Alternatively, you can view logs locally by entering the following in your terminal from within your project's directory:

```bash
firebase functions:log [function-name]
```

Replace `function-name` with the name of the function you'd like to view logs for. You can add an optional `-f` flag to see logs as they are written.

> To view logs locally you'll need to have [gcloud](https://cloud.google.com/sdk/) installed and have run `gcloud auth login`.


## Contributing

We'd love that you contribute to the project. Before doing so please read our [Contributor guide](../CONTRIBUTING.md).


## License

© Google, 2015. Licensed under an [Apache-2](../LICENSE) license.
