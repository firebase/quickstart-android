package com.google.samples.quickstart.config.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RemoteConfigViewModel(
    private val remoteConfig: FirebaseRemoteConfig
) : ViewModel() {
    private val _welcomeMessage = MutableStateFlow("Welcome...")
    val welcomeMessage: StateFlow<String> = _welcomeMessage

    private val _allCaps = MutableStateFlow(false)
    val allCaps: StateFlow<Boolean> = _allCaps

    fun enableDeveloperMode() {
        viewModelScope.launch {
            // Create a Remote Config Setting to enable developer mode, which you can use to increase
            // the number of fetches available per hour during development. Also use Remote Config
            // Setting to set the minimum fetch interval.
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings).await()
        }
    }

    fun setDefaultValues(defaultValuesXml: Int) {
        viewModelScope.launch {
            // Set default Remote Config parameter values. An app uses the in-app default values, and
            // when you need to adjust those defaults, you set an updated value for only the values you
            // want to change in the Firebase console. See Best Practices in the README for more
            // information.
            remoteConfig.setDefaultsAsync(defaultValuesXml).await()

            // Update the UI with the default parameter values
            updateUI()
        }
    }

    fun fetchRemoteConfig() {
        _welcomeMessage.value = remoteConfig[LOADING_PHRASE_CONFIG_KEY].asString()

        viewModelScope.launch {
            try {
                val updated = remoteConfig.fetchAndActivate().await()
                Log.d(TAG, "Config params updated: $updated")

                // Update the UI with the fetched parameter values
                updateUI()
            } catch (e: Exception) {
                Log.e(TAG, "There was an error fetching and activating your config")
                _welcomeMessage.value = e.message ?: "Unknown Error"
            }
        }
    }

    private fun updateUI() {
        _welcomeMessage.value = remoteConfig[WELCOME_MESSAGE_KEY].asString()
        _allCaps.value = remoteConfig[WELCOME_MESSAGE_CAPS_KEY].asBoolean()
    }

    companion object {
        const val TAG = "RemoteConfigViewModel"

        // Remote Config keys
        private const val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
        private const val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"

        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get Remote Config instance.
                val remoteConfig = Firebase.remoteConfig
                return RemoteConfigViewModel(remoteConfig) as T
            }
        }
    }
}
