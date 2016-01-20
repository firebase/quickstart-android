/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.database;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import com.firebase.client.Firebase;

/**
 * Android application subclass to enable us to attach the Firebase handler to
 * the Context in the onCreate. This ensures we have established the reference
 * before making any database calls elsewhere in the application.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
            new FirebaseOptions(getString(R.string.google_crash_reporting_api_key)));
    }
}
