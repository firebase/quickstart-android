# Firebase Functions Quickstart - Android

This quickstart demonstrates using **Google Cloud Functions** and its interaction with a **Firebase database** through a simple **Android app**.


## Introduction

This sample adds a Cloud Function that automatically makes all messages uppercase as soon as they are inserted in a Firebase database. This quickstart can be followed on its own but the Android Firebase Database quickstart app can serve as a UI for this sample so - ideally - please go through the [Android Firebase Database quickstart](../database/) first.

Further reading:

 - [Read more about Firebase Functions](https://developers.google.com/firebase/docs/functions/)


## Initial setup, build tools and dependencies

### 1. Clone this repo

Clone this repo and open the `android/functions` directory:
 - Open https://www.googlesource.com/new-password in your browser and run the generated script in your terminal.
 - Then, run the following command:

```bash
git clone https://dev-partners.googlesource.com/samples/firebase/quickstart/android/
cd android/functions
```


### 2. Create a Firebase project and configure the quickstart

Create a Firebase Project on the [Firebase Console](http://g.co/firebase).

Note your Firebase **database URL** and your **Project ID**. You can find your Firebase database URL in the **Database** section. It looks like:
`https://<YOUR_PROJECT_ID>.firebaseio.com/`.

Create a Browser API Key on your project:

 - Open [this page](https://console.developers.google.com/apis/credentials/key?type=CLIENT_SIDE&project=_).
 - Select your project (e.g. `functions-quickstart-12345`) and click **Continue**.
 - Enter a name and the URLs that you’d like to whitelist. These should be `localhost:5000` (used for local development) and `<PROJECT_ID>.firebaseapp.com` (e.g. `functions-quickstart-12345.firebaseapp.com` which is your Firebase static hosting URL).
 - Click **Create**.
 - Note your **API Key** (e.g. `AIzaSyBvhcMB9my5Uuu_wqpqCNfDjyqoHKKX3yM`).

Use these values to replace `<DATABASE_URL>` and `<API_KEY>` in `scripts/main.js`.

Set up your Firebase project by running `firebase use --add`, select your Project ID and follow the instructions.


### 3. Enable billing and the Google Cloud Functions APIs

Enable Billing on your project:

In your project's **Firebase Console** upgrade to one of the paid plans using the **UPGRADE** button.

Enable the Google Cloud APIs required to run Firebase Functions on your project:

 - Open [this page](https://console.developers.google.com/flows/enableapi?apiid=cloudfunctions).
 - Choose your project (e.g. `functions-quickstart-12345`) and click **Continue**.


### 4. Install the Firebase CLI Alpha

In your project's **Firebase Console** go to the **Hosting tab**, click **GET STARTED** and follow the instructions to download and install the CLI.

> To install the CLI you need to have installed [npm](https://www.npmjs.com/) which typically comes with [Node.js](https://nodejs.org).

To verify that the CLI has been installed correctly open a console and run:

```bash
$~> firebase version
x.x.x
```

Authorize the Firebase CLI by running:

```bash
$~> firebase login
```

Setup the Firebase CLI to use your Firebase Project:

```bash
$~> firebase use --add
```

Then select your Project ID and follow the instructions.


## Deploy the app to prod

First you need to install the `npm` dependencies of the functions:

```bash
cd functions && npm install; cd ..
```

This installs locally the Firebase SDK and the Firebase Functions SDK.

Deploy to Firebase using the following command:

```bash
firebase deploy
```

This deploys and activate the Function that will make all of your messages uppercase.

> The first time you call `firebase deploy` on a new project the Google Compute Engine instances and Kubernetes clusters required to run Firebase Functions will be spin-up. This may take a few minutes but things will be a lot faster on subsequent deploys. If you get an error try again after waiting a few minutes.

## Data Model

In this sample we use the following data model:

```
\functions-quickstart-12345
    \messages
        \key-123456
            text: "This is my first message!"
        \key-123457
            text: "This is my second message!"
```

 - `\functions-quickstart-12345\messages` is a list of Objects. New messages get pushed in this list.
 - A message is an Object with one attribute: `text`. We chose this instead of a simple String for consistency with the other Firebase samples and codelabs so that you can easily combine them.

## See the results

Once your Function is deployed add the following objects to your Firebase Database manually using the Firebase Console:

```
\functions-quickstart-12345
    \messages
        \key-123456
            text: "This is my first message!"
        \key-123457
            text: "This is my second message!"
```

Adding these objects triggers the function which makes the messages uppercase:

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

Within Firebase Functions any `console.log()` statements are logged to Cloud Logging. You can view these logs by using [this Cloud Logging filter](https://console.developers.google.com/logs?project=_&service=cloudfunctions.googleapis.com).

Alternatively, you can view logs locally by entering the following in your terminal from within your project's directory:

```bash
firebase functions:log [function-name]
```

Replace `function-name` with the name of the function you'd like to view logs for. You can add an optional `-f` flag to see logs as they are written.

> To view logs locally you'll need to have [gcloud](https://cloud.google.com/sdk/) installed and have run `gcloud auth login`.


## Contributing

We'd love that you contribute to the project. Before doing so please read our [Contributor guide](../CONTRIBUTING.md).


## License

© Google, 2016. Licensed under an [Apache-2](../LICENSE) license.
