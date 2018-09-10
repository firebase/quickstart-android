package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.samples.quickstart.config.BuildConfig
import com.google.samples.quickstart.config.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchButton.setOnClickListener { fetchWelcome() }

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. See Best Practices in the
        // README for more information.
        // [START enable_dev_mode]
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
        // [END set_default_values]

        fetchWelcome()
    }

    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */
    private fun fetchWelcome() {
        welcomeTextView.text = mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY)

        val isUsingDeveloperMode = mFirebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled

        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        val cacheExpiration: Long = if (isUsingDeveloperMode) {
            0
        } else {
            3600 // 1 hour in seconds.
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Fetch Succeeded",
                                Toast.LENGTH_SHORT).show()

                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig.activateFetched()
                    } else {
                        Toast.makeText(this, "Fetch Failed",
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
        val welcomeMessage = mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY)
        // [END get_config_values]
        if (mFirebaseRemoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY)) {
            welcomeTextView.setAllCaps(true)
        } else {
            welcomeTextView.setAllCaps(false)
        }
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