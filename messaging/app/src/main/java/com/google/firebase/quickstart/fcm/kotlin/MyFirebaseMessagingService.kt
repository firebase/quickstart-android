package com.google.firebase.quickstart.fcm.kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.quickstart.fcm.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Check if data needs to be processed by long running job
            if (isLongRunningJob()) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            it.body?.let { body -> sendNotification(body) }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private fun isLongRunningJob() = true

  // [START onRegistered]
  /**
   * Called when the current app instance has been successfully registered with FCM.
   *
   *
   * This method provides the unique Firebase Installation ID (FID), which should be used to
   * target this app instance for direct-send messaging.
   *
   *
   * This callback is triggered in the following scenarios:
   *
   *
   *  * When the registration first succeeds after app install (if auto-init is enabled).
   *  * When the registration is refreshed due to invalidation or updates (if auto-init is
   * enabled).
   *  * Immediately after a direct call to [FirebaseMessaging.register].
   *
   *
   *
   * Ensure the provided `installationId` is uploaded if it hasn't been previously or it might
   * have been deleted on 404s.
   *
   *
   * **Note:** To use this API, you must enable it by adding `<meta-data android:name="firebase_messaging_installation_id_enabled" android:value="true" />` to your
   * app's manifest.
   *
   * @param installationId The Firebase Installation ID used for sending messages to the current app
   * instance.
   */
  public override fun onRegistered(installationId: String) {
    Log.d(TAG, "Registration: " + installationId)

    // If you want to send messages to this application instance or
    // manage these apps subscriptions on the server side, send the
    // FCM registration to your app server.
    sendRegistrationToServer(installationId)

    // Log and toast
    val msg = getString(R.string.msg_registration_fmt, installationId)
    Log.d(TAG, msg)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
  }

  // [END onRegistered]

  // [START onRegistered]
  /**
   * Called when the current app instance has been successfully unregistered from FCM via a call to
   * `FirebaseMessaging.unregister()`.
   *
   *
   * This method confirms that the specified FID is no longer active for receiving FCM messages.
   *
   *
   * **Note:** To use this API, you must enable it by adding `<meta-data android:name="firebase_messaging_installation_id_enabled" android:value="true" />` to your
   * app's manifest.
   *
   * @param installationId The Firebase Installation ID of the current app instance that was
   * unregistered with FCM.
   */
  public override fun onUnregistered(installationId: String) {
    super.onUnregistered(installationId)
    // Remove FCM registration associated with the app instance on the app server
    // so that the app server will not try to send FCM messages to the un registered app instance.
    removeRegistrationFromServer(installationId)
  }

  /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

  /**
   * Persist registration to third-party servers.
   *
   * Modify this method to associate the user's FCM registration with any
   * server-side account maintained by your application.
   *
   * @param registration The new FCM registration.
   */
  private fun sendRegistrationToServer(registration: String?) {
    // TODO: Implement this method to send FCM registration to your app server.
  }

  /**
   * Remove registration from third-party servers.
   *
   * Modify this method to disassociate the user's FCM registration with any
   * server-side account maintained by your application.
   *
   * @param registration The FCM registration which is unregistered.
   */
  private fun removeRegistrationFromServer(registration: String?) {
    // TODO: Implement this method to remove FCM registration from your app server.
  }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val requestCode = 0
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(getString(R.string.fcm_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}
