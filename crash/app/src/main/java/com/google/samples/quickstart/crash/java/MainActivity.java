/*
 * Copyright Google Inc. All Rights Reserved.
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

package com.google.samples.quickstart.crash.java;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.samples.quickstart.crash.databinding.ActivityMainBinding;

/**
 * This Activity shows the different ways of reporting application crashes.
 * - Report non-fatal exceptions that are caught by your app.
 * - Automatically Report uncaught crashes.
 *
 * It also shows how to add log messages to crash reports using log().
 *
 * Check https://console.firebase.google.com to view and analyze your crash reports.
 *
 * Check https://firebase.google.com/docs/crashlytics for more information on Firebase Crashlytics.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseCrashlytics mCrashlytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mCrashlytics = FirebaseCrashlytics.getInstance();

        // Log the onCreate event, this will also be printed in logcat
        mCrashlytics.log("onCreate");

        // Add some custom values and identifiers to be included in crash reports
        mCrashlytics.setCustomKey("MeaningOfLife", 42);
        mCrashlytics.setCustomKey("LastUIAction", "Test value");
        mCrashlytics.setUserId("123456789");

        // Report a non-fatal exception, for demonstration purposes
        mCrashlytics.recordException(new Exception("Non-fatal exception: something went wrong!"));

        // Button that causes NullPointerException to be thrown.
        binding.crashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log that crash button was clicked.
                mCrashlytics.log("Crash button clicked.");

                // If catchCrashCheckBox is checked catch the exception and report it using
                // logException(), Otherwise throw the exception and let Crashlytics automatically
                // report the crash.
                if (binding.catchCrashCheckBox.isChecked()) {
                    try {
                        throw new NullPointerException();
                    } catch (NullPointerException ex) {
                        // [START crashlytics_log_and_report]
                        mCrashlytics.log("NPE caught!");
                        mCrashlytics.recordException(ex);
                        // [END crashlytics_log_and_report]
                    }
                } else {
                    throw new NullPointerException();
                }
            }
        });

        // Log that the Activity was created.
        // [START crashlytics_log_event]
        mCrashlytics.log("Activity created");
        // [END crashlytics_log_event]
    }
}
