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

package com.google.samples.quickstart.crash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import com.google.firebase.crash.FirebaseCrash

/**
 * This Activity shows the different ways of reporting application crashes.
 * - Report caught crashes with Crash.report().
 * - Automatically Report uncaught crashes.

 * It also shows how to add log messages to crash reports using Crash.log().

 * Check https://console.firebase.google.com to view and analyze your crash reports.

 * Check https://firebase.google.com/docs/crash/android for more on
 * Firebase Crash on Android.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Checkbox to indicate when to catch the thrown exception.
        val catchCrashCheckBox = findViewById(R.id.catchCrashCheckBox) as CheckBox

        // Button that causes the NullPointerException to be thrown.
        val crashButton = findViewById(R.id.crashButton) as Button
        crashButton.setOnClickListener {
            // Log that crash button was clicked. This version of Crash.log() will include the
            // message in the crash report as well as show the message in logcat.
            FirebaseCrash.logcat(Log.INFO, TAG, "Crash button clicked")

            // If catchCrashCheckBox is checked catch the exception and report is using
            // Crash.report(). Otherwise throw the exception and let Firebase Crash automatically
            // report the crash.
            if (catchCrashCheckBox.isChecked) {
                try {
                    throw NullPointerException()
                } catch (ex: NullPointerException) {
                    // [START log_and_report]
                    FirebaseCrash.logcat(Log.ERROR, TAG, "NPE caught")
                    FirebaseCrash.report(ex)
                    // [END log_and_report]
                }

            } else {
                throw NullPointerException()
            }
        }

        // Log that the Activity was created. This version of Crash.log() will include the message
        // in the crash report but will not be shown in logcat.
        // [START log_event]
        FirebaseCrash.log("Activity created")
        // [END log_event]
    }

    companion object {

        private val TAG = "MainActivity"

    }
}
