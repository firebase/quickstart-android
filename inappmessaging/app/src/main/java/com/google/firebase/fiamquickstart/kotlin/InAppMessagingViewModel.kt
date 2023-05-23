package com.google.firebase.fiamquickstart.kotlin

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.ktx.installations
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InAppMessagingViewModel(
    private val firebaseAnalytics: FirebaseAnalytics,
    firebaseIam: FirebaseInAppMessaging,
    private val firebaseInstallations: FirebaseInstallations,
): ViewModel() {

    private val _installationsId = MutableStateFlow("")
    val installationsId: StateFlow<String> = _installationsId

    init {
        firebaseIam.isAutomaticDataCollectionEnabled = true
        firebaseIam.setMessagesSuppressed(false)

        viewModelScope.launch {
            try {
                val id = firebaseInstallations.id.await()
                _installationsId.value = id
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    fun triggerEvent(){
        firebaseAnalytics.logEvent("engagement_party", Bundle())
    }

    companion object {
        const val TAG = "InAppMessagingViewModel"

        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val firebaseAnalytics = Firebase.analytics
                val firebaseIam = Firebase.inAppMessaging
                val firebaseInstallations = Firebase.installations
                return InAppMessagingViewModel(firebaseAnalytics, firebaseIam, firebaseInstallations) as T
            }
        }
    }
}