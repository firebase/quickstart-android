package com.google.firebase.quickstart.fcm.kotlin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.quickstart.fcm.kotlin.data.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseMessagingViewModel(
    private val messaging: FirebaseMessaging
): ViewModel() {

    private var _token: MutableStateFlow<String> = MutableStateFlow("")
    val token: MutableStateFlow<String> = _token

    private var _subscriptionState = MutableStateFlow(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    fun askNotificationPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun setNotificationChannel(context: Context, channelId: String, channelName: String ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    fun logNotificationData(intent: Intent) {
        intent.extras?.let {
            it.keySet().forEach { key ->
                val value = intent.extras?.getString(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
    }

    fun getToken() {
        viewModelScope.launch {
            try {
                _token.value = messaging.getToken().await()
                Log.d(TAG, "FCM registration Token: ${_token.value}")
            } catch(e: Exception) {
                Log.w(TAG, "Fetching FCM registration token failed", e)
            }
        }
    }

    fun subscribeToTopic(topicName: String) {
        viewModelScope.launch {
            Log.d(TAG, "Subscribing to topic: $topicName")
            try {
                messaging.subscribeToTopic(topicName).await()
                Log.d(TAG, "Subscribed to $topicName topic")
                _subscriptionState.value = SubscriptionState.Success
            } catch (e: Exception) {
                Log.w(TAG, "Failed to subscribe to $topicName", e)
                _subscriptionState.value = SubscriptionState.Failed
            }
        }
    }

    companion object {
        const val TAG = "FCMViewModel"
        // Used to inject this ViewModel's dependencies
        // See also: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val firebaseMessaging = FirebaseMessaging.getInstance()
                return FirebaseMessagingViewModel(firebaseMessaging) as T
            }
        }
    }
}