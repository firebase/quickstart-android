package com.google.samples.quickstart.config.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RemoteConfigViewModel(
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
) : ViewModel() {
    private val _welcomeMessage = MutableStateFlow(remoteConfig[WELCOME_MESSAGE_KEY].asString())
    val welcomeMessage: StateFlow<String> = _welcomeMessage

    fun fetchConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    _welcomeMessage.value = remoteConfig[WELCOME_MESSAGE_KEY].asString()
                } else {
                    _welcomeMessage.value = task.exception?.message ?: "Unknown Error"
                }
            }
    }

    companion object {
        const val TAG = "ConfigViewModel"
        private const val WELCOME_MESSAGE_KEY = "welcome_message"
    }
}