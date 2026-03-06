package com.google.samples.quickstart.functions.kotlin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.samples.quickstart.functions.R

class FunctionsMessagingService : FirebaseMessagingService() {

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Messages", "Messages", importance)
            channel.description = "All messages."
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        createNotificationChannel()

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Check if permission to post notifications has been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val manager = NotificationManagerCompat.from(this)
                val notification = NotificationCompat.Builder(this, "Messages")
                    .setContentText(remoteMessage.data["text"])
                    .setContentTitle("New message")
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .build()
                manager.notify(0, notification)
            }
        }
    }

    companion object {
        private const val TAG = "MessagingService"
    }
}
