package com.google.samples.quickstart.crash.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.samples.quickstart.crash.R
import kotlinx.android.synthetic.main.activity_main.*

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
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Log the onCreate event, this will also be printed in logcat
        Crashlytics.log(Log.VERBOSE, TAG, "onCreate")

        // Add some custom values and identifiers to be included in crash reports
        Crashlytics.setInt("MeaningOfLife", 42)
        Crashlytics.setString("LastUIAction", "Test value")
        Crashlytics.setUserIdentifier("123456789")

        // Report a non-fatal exception, for demonstration purposes
        Crashlytics.logException(Exception("Non-fatal exception: something went wrong!"))

        // Checkbox to indicate when to catch the thrown exception.
        val catchCrashCheckBox = catchCrashCheckBox

        // Button that causes NullPointerException to be thrown.
        val crashButton = crashButton
        crashButton.setOnClickListener {
            // Log that crash button was clicked.
            Crashlytics.log(Log.INFO, TAG, "Crash button clicked.")

            // If catchCrashCheckBox is checked catch the exception and report is using
            // logException(), Otherwise throw the exception and let Crashlytics automatically
            // report the crash.
            if (catchCrashCheckBox.isChecked) {
                try {
                    throw NullPointerException()
                } catch (ex: NullPointerException) {
                    // [START crashlytics_log_and_report]
                    Crashlytics.log(Log.ERROR, TAG, "NPE caught!")
                    Crashlytics.logException(ex)
                    // [END crashlytics_log_and_report]
                }
            } else {
                throw NullPointerException()
            }
        }

        // Log that the Activity was created.
        // [START crashlytics_log_event]
        Crashlytics.log("Activity created")
        // [END crashlytics_log_event]
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
