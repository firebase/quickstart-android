package com.google.samples.quickstart.config.kotlin

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.samples.quickstart.config.R

class FirebaseActivity {
    companion object {

        private const val TAG = "FirebaseActivity"

        // Remote Config keys
        private const val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
        private const val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"

        // init config
        fun initializeConfig() : FirebaseRemoteConfig {
            // Get Remote Config instance.
            // [START get_remote_config_instance]
            val remoteConfig = Firebase.remoteConfig
            // [END get_remote_config_instance]

            // Create a Remote Config Setting to enable developer mode, which you can use to increase
            // the number of fetches available per hour during development. Also use Remote Config
            // Setting to set the minimum fetch interval.
            // [START enable_dev_mode]
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            // [END enable_dev_mode]

            // Set default Remote Config parameter values. An app uses the in-app default values, and
            // when you need to adjust those defaults, you set an updated value for only the values you
            // want to change in the Firebase console. See Best Practices in the README for more
            // information.
            // [START set_default_values]
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            // [END set_default_values]
            return remoteConfig
        }

        // fetch msg

        // Receives the remoteConfig to fetch a message
        fun fetchConfig(activity : Activity, remoteConfig : FirebaseRemoteConfig) : String{

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d(TAG, "Config params updated: $updated")
                        Toast.makeText(activity, "Fetch and activate succeeded",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Fetch failed",
                            Toast.LENGTH_SHORT).show()
                    }
                }

            return remoteConfig[WELCOME_MESSAGE_KEY].asString()
        }




    }
}