package com.google.firebase.quickstart.fcm.kotlin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.quickstart.fcm.R
import com.google.firebase.quickstart.fcm.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        // [END handle_data_extras]

        binding.subscribeButton.setOnClickListener {
            Log.d(TAG, "Subscribing to weather topic")
            // [START subscribe_topics]
            Firebase.messaging.subscribeToTopic("weather")
                    .addOnCompleteListener { task ->
                        var msg = getString(R.string.msg_subscribed)
                        if (!task.isSuccessful) {
                            msg = getString(R.string.msg_subscribe_failed)
                        }
                        Log.d(TAG, msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
            // [END subscribe_topics]
        }

        binding.logTokenButton.setOnClickListener {
            // Get token
            lifecycleScope.launch {
                // Get new FCM registration token
                val token = getAndStoreRegToken()
                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
        }

        Toast.makeText(this, "See README for setup instructions", Toast.LENGTH_SHORT).show()
        askNotificationPermission()


        dateRefresh()
        optimizedDateRefresh() // optimized version of dateRefresh() that requires using Android SharedPreferences
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private suspend fun getAndStoreRegToken(): String {
        // [START log_reg_token]
        var token = Firebase.messaging.token.await()
        // Add token and timestamp to Firestore for this user
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp(),
        )

        // Get user ID from Firebase Auth or your own server
        Firebase.firestore.collection("fcmTokens").document("myuserid")
            .set(deviceToken).await()
        // [END log_reg_token]
        Log.d(TAG, "got token: $token")

        // As an optimization, store today’s date in Android cache
        val preferences = this.getSharedPreferences("default", Context.MODE_PRIVATE)
        preferences.edit().putLong("lastDeviceRefreshDate", Date().time)

        return token
    }

    // Check to see whether this device's registration token was refreshed within the last month. Refresh if not.
    private fun dateRefresh() {
        lifecycleScope.launch {
            val refreshDate = (Firebase.firestore.collection("refresh")
                    .document("refreshDate").get().await().data!!["lastRefreshDate"] as Timestamp)
            val deviceRefreshDate = (Firebase.firestore.collection("fcmTokens")
                    .document("myuserid").get().await().data!!["timestamp"] as Timestamp)
            if (deviceRefreshDate < refreshDate) {
                getAndStoreRegToken()
            }
        }
    }

    /*
        As an optimization to prevent Firestore calls every time the device opens the app, store the last all-devices
        refresh date (lastGlobalRefresh) and this particular device's last refresh date (lastDeviceRefresh) into
        Android's SharedPreferences.

        If lastDeviceRefresh is before lastGlobalRefresh, update the device's registration token, and store it into
        Firestore and SharedPreferencs. Also, if today's date is a month after lastGlobalRefresh, sync lastGlobalRefresh
        in SharedPreferences with Firestore's lastGlobalRefresh.
    */
    private fun optimizedDateRefresh() {
        val preferences = this.getPreferences(Context.MODE_PRIVATE)
        // Refresh date (stored as milliseconds, SharedPreferences cannot store Date) that ensures token freshness
        val lastGlobalRefreshLong = preferences.getLong("lastGlobalRefreshDate", -1)
        val lastGlobalRefresh = Date(lastGlobalRefreshLong)
        // Date of last refresh of device’s registration token
        val lastDeviceRefreshLong = preferences.getLong("lastDeviceRefreshDate", -1)
        val lastDeviceRefresh = Date(lastDeviceRefreshLong)
        lifecycleScope.launch {
            if (lastDeviceRefreshLong == -1L || lastGlobalRefreshLong == -1L
                || lastDeviceRefresh.before(lastGlobalRefresh)) {
                // Get token, store into Firestore, and update cache
                getAndStoreRegToken()
                preferences.edit().putLong("lastGlobalRefreshDate", lastDeviceRefresh.time)
            }

            // Check if today is more than one month beyond cached global refresh date
            // and if so, sync date with Firestore and update cache
            val today = Date()
            val c = Calendar.getInstance().apply {
                time = if (lastGlobalRefreshLong == -1L) today else lastGlobalRefresh
                add(Calendar.DATE, 30)
            }

            if (lastGlobalRefreshLong == -1L || today.after(c.time)) {
                val document = Firebase.firestore.collection("refresh").document("refreshDate").get().await()
                val updatedTime = (document.data!!["lastRefreshDate"] as Timestamp).seconds * 1000
                preferences.edit().putLong("lastGlobalRefreshDate", updatedTime)
            }
        }
    }

    companion object {

        private const val TAG = "MainActivityandreawu"
    }
}
