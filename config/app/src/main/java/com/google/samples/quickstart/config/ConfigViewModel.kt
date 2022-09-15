package com.google.samples.quickstart.config

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConfigViewModel(
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
) : ViewModel() {

    private val _activated = MutableStateFlow(false)
    val activated: StateFlow<Boolean>
        get() = _activated

    private val _welcomeMessage = MutableStateFlow("")
    val welcomeMessage: StateFlow<String>
        get() = _welcomeMessage

    private val _allCaps = MutableStateFlow(true)
    val allCaps: StateFlow<Boolean>
        get() = _allCaps

    init {
        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // Used for development
        })

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun fetchAndActivateConfig() {
        _welcomeMessage.value = remoteConfig.getString(LOADING_PHRASE_CONFIG_KEY)

        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            // Display any errors on the UI
            _welcomeMessage.value = throwable.message ?: "Unknown Error occurred"
        }) {
            _activated.value = remoteConfig.fetchAndActivate().await()

            _welcomeMessage.value = remoteConfig.getString(WELCOME_MESSAGE_KEY)
            _allCaps.value = remoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY)
        }
    }

    companion object {
        // Remote Config keys
        private const val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
        private const val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"
    }
}