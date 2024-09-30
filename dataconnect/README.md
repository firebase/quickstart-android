# Firebase Data Connect Quickstart

## Introduction

This quickstart is a movie review app to demonstrate the use of Firebase Data Connect
 with a Cloud SQL database.
For more information about Firebase Data Connect visit [the docs](https://firebase.google.com/docs/data-connect/).

## Getting Started

Follow these steps to get up and running with Firebase Data Connect. For more detailed instructions,
check out the [official documentation](https://firebase.google.com/docs/data-connect/quickstart).

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

5. If you haven’t already, add an Android app to your Firebase project, with the android package name `com.google.firebase.example.dataconnect`. Download and then add the Firebase Android configuration file (`google-services.json`) to your app:
    1. Click **Download google-services.json** to obtain your Firebase Android config file.
    2. Move your config file into the `quickstart-android/dataconnect/app` directory.

### 2. Set Up Firebase CLI

Ensure the Firebase CLI is installed and up to date:

```bash
npm install -g firebase-tools
```

### 3. Cloning the repository
This repository contains the quickstart to get started with the functionalities of Data Connect.

1. Clone this repository to your local machine:
   ```sh
   git clone https://github.com/firebase/quickstart-android.git
   ```

2. (Private Preview only) Checkout the `fdc-quickstart` branch (`git checkout fdc-quickstart`)
   and open the project in Android Studio.

### 4. Deploy the service to Firebase and generate SDKs

1. Open the `quickstart-android/dataconnect/dataconnect` directory and deploy the schema with
    the following command:
    ```bash
    firebase deploy
    ```
2. Once the deploy is complete, you should be able to see the movie schema in the
 [Data Connect section](https://console.firebase.google.com/u/0/project/_/dataconnect)
 of the Firebase console.

3. Generate the Kotlin SDK by running:
   ```bash
   firebase dataconnect:sdk:generate
   ```

### 5. Populating the database
1. Run `1_movie_insert.gql`, `2_actor_insert.gql`, `3_movie_actor_insert.gql`, and `4_user_favorites_review_insert.gql` files in the `./dataconnect` directory in order using the VS code extension,

### 6. Running the app

Press the Run button in Android Studio to run the sample app on your device.

## 🚧 Work in Progress

This app is still missing some features which will be added before Public Preview:

- [ ] Search
- [ ] Movie review
  - [x] Add a new review 
  - [ ] Update a review
  - [ ] Delete a review
- [x] Actors
  - [x] Show actor profile
  - [x] Mark actor as favorite
- [ ] Error handling
  Some errors may cause the app to crash, especially if there's no user logged in. 
