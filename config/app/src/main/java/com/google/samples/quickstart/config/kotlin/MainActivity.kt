package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.samples.quickstart.config.R
import kotlinx.android.synthetic.main.activity_main.fetchButton
import kotlinx.android.synthetic.main.activity_main.welcomeTextView

class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchButton.setOnClickListener { fetchWelcome() }

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        remoteConfig = FirebaseRemoteConfig.getInstance()
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // [END set_default_values]

        fetchWelcome()
    }

    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */
    private fun fetchWelcome() {
        welcomeTextView.text = remoteConfig.getString(LOADING_PHRASE_CONFIG_KEY)

        // [START fetch_config_with_callback]
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d(TAG, "Config params updated: $updated")
                        Toast.makeText(this, "Fetch and activate succeeded",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Fetch failed",
                                Toast.LENGTH_SHORT).show()
                    }
                    displayWelcomeMessage()
                }
        // [END fetch_config_with_callback]
    }

    /**
     * Display a welcome message in all caps if welcome_message_caps is set to true. Otherwise,
     * display a welcome message as fetched from welcome_message.
     */
    // [START display_welcome_message]
    private fun displayWelcomeMessage() {
        // [START get_config_values]
        val welcomeMessage = remoteConfig.getString(WELCOME_MESSAGE_KEY)
        // [END get_config_values]
        welcomeTextView.isAllCaps = remoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY)
        welcomeTextView.text = welcomeMessage
    }

    companion object {

        private const val TAG = "MainActivity"

        // Remote Config keys
        private const val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
        private const val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"
    }
    // [END display_welcome_message]
}
