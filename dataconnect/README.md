# Firebase Data Connect Quickstart

## Introduction

This quickstart is a movie review app to demonstrate the use of Firebase Data Connect
 with a Cloud SQL database.
For more information about Firebase Data Connect visit [the docs](https://firebase.google.com/docs/data-connect/).

## Getting Started

Follow these steps to get up and running with Firebase Data Connect. For more detailed instructions,
check out the [official documentation](https://firebase.google.com/docs/data-connect/quickstart).

### 0. Prerequisites
- Latest version of [Android Studio](https://developer.android.com/studio)
- Latest version of [Visual Studio Code](https://code.visualstudio.com/)
- The [Firebase Data Connect VS Code Extension](https://marketplace.visualstudio.com/items?itemName=GoogleCloudTools.firebase-dataconnect-vscode)

### 1. Connect to your Firebase project

1. If you haven't already, create a Firebase project.
    1. In the [Firebase console](https://console.firebase.google.com), click
        **Add project**, then follow the on-screen instructions.

2. Upgrade your project to the Blaze plan. This lets you create a Cloud SQL
    for PostgreSQL instance.

    > Note: Though you set up billing in your Blaze upgrade, you won't be
    charged for usage of Firebase Data Connect or the
    [default Cloud SQL for PostgreSQL configuration](https://firebase.google.com/docs/data-connect/#pricing)
    during the preview.

3. Navigate to the [Data Connect section](https://console.firebase.google.com/u/0/project/_/dataconnect)
    of the Firebase console, click on the "Get Started" button and follow the setup workflow:
     - Select a location for your Cloud SQL for PostgreSQL database (this sample uses `us-central1`). If you choose a different location, you'll also need to change the `quickstart-android/dataconnect/dataconnect/dataconnect.yaml` file. 
     - Select the option to create a new Cloud SQL instance and fill in the following fields:
       - Service ID: `dataconnect`
       - Cloud SQL Instance ID: `fdc-sql`
       - Database name: `fdcdb`
4. Allow some time for the Cloud SQL instance to be provisioned. After it's provisioned, the instance
   can be managed in the [Cloud Console](https://console.cloud.google.com/sql).

5. If you haven’t already, add an Android app to your Firebase project, with the android package name `com.google.firebase.example.dataconnect`.
 Click **Download google-services.json** to obtain your Firebase Android config file.

### 2. Cloning the repository

1. Clone this repository to your local machine:
   ```sh
   git clone https://github.com/firebase/quickstart-android.git
   ```

2. Move the `google-services.json` config file (downloaded in the previous step) into the
  `quickstart-android/dataconnect/app/` directory.

### 3. Open in Visual Studio Code (VS Code)

1. Open the `quickstart-android/dataconnect` directory in VS Code.
2. Click on the Firebase Data Connect icon on the VS Code sidebar to load the Extension.
   a. Sign in with your Google Account if you haven't already.
3. Click on "Connect a Firebase project" and choose the project where you have set up Data Connect.
4. Click on "Start Emulators" - this should generate the Kotlin SDK for you and start the emulators.

### 4. Populate the database
In VS Code, open the `quickstart-android/dataconnect/dataconnect/moviedata_insert.gql` file and click the
 `Run (local)` button at the top of the file.

If you’d like to confirm that the data was correctly inserted,
open `quickstart-android/dataconnect/movie-connector/queries.gql` and run the `ListMovies` query.

### 5. Running the app

Press the Run button in Android Studio to run the sample app on your device.
