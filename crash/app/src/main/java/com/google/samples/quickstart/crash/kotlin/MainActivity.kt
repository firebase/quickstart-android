package com.google.samples.quickstart.crash.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.samples.quickstart.crash.databinding.ActivityMainBinding

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

    private lateinit var crashlytics: FirebaseCrashlytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crashlytics = Firebase.crashlytics

        // Log the onCreate event, this will also be printed in logcat
        crashlytics.log("onCreate")

        // Add some custom values and identifiers to be included in crash reports
        crashlytics.setCustomKeys {
            key("MeaningOfLife", 42)
            key("LastUIAction", "Test value")
        }
        crashlytics.setUserId("123456789")

        // Report a non-fatal exception, for demonstration purposes
        crashlytics.recordException(Exception("Non-fatal exception: something went wrong!"))

        // Button that causes NullPointerException to be thrown.
        binding.crashButton.setOnClickListener {
            // Log that crash button was clicked.
            crashlytics.log("Crash button clicked.")

            // If catchCrashCheckBox is checked catch the exception and report it using
            // logException(), Otherwise throw the exception and let Crashlytics automatically
            // report the crash.
            if (binding.catchCrashCheckBox.isChecked) {
                try {
                    throw NullPointerException()
                } catch (ex: NullPointerException) {
                    // [START crashlytics_log_and_report]
                    crashlytics.log("NPE caught!")
                    crashlytics.recordException(ex)
                    // [END crashlytics_log_and_report]
                }
            } else {
                throw NullPointerException()
            }
        }

        // Log that the Activity was created.
        // [START crashlytics_log_event]
        crashlytics.log("Activity created")
        // [END crashlytics_log_event]
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
