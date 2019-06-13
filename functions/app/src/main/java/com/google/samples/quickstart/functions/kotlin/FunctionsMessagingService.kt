package com.google.samples.quickstart.functions.kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
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
            val manager = NotificationManagerCompat.from(this)
            val notification = NotificationCompat.Builder(this, "Messages")
                    .setContentText(remoteMessage.data["text"])
                    .setContentTitle("New message")
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .build()
            manager.notify(0, notification)
        }
    }

    companion object {
        private const val TAG = "MessagingService"
    }
}
