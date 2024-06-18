package com.google.firebase.quickstart.fcm.kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.quickstart.fcm.kotlin.data.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FirebaseMessagingViewModel(
    private val messaging: FirebaseMessaging
): ViewModel() {

    private var _token: MutableStateFlow<String> = MutableStateFlow("")
    val token: MutableStateFlow<String> = _token

    private var _subscriptionState = MutableStateFlow(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

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

    fun getNotificationData(intent: Intent) {
        viewModelScope.launch {
            intent.extras?.let {
                it.keySet().forEach { key ->
                    val value = intent.extras?.getString(key)
                    Log.d(TAG, "Key: $key Value: $value")
                }
            }
        }
    }

    fun getToken() {
        viewModelScope.launch {
            messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                _token.value = task.result
                Log.d(TAG, "FCM registration Token: " + task.result)
            })
        }
    }

    fun getSubscribe(topicName: String) {
        viewModelScope.launch {
            Log.d(TAG, "Subscribing to topic: $topicName")
            _subscriptionState.value = SubscriptionState.Loading
            messaging.subscribeToTopic(topicName)
                .addOnCompleteListener { task ->
                    _subscriptionState.value = when {
                        !task.isSuccessful -> SubscriptionState.Failed
                        else -> {
                            Log.d(TAG, "Subscribed to weather topic")
                            SubscriptionState.Success
                        }
                    }
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