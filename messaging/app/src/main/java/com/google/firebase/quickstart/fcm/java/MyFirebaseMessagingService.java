/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.fcm.java;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.quickstart.fcm.R;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String notificationBody = remoteMessage.getNotification().getBody();
            if (remoteMessage.getNotification().getBody() != null) {
                sendNotification(notificationBody);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START onRegistered]
  /**
   * Called when the current app instance has been successfully registered with FCM.
   *
   * <p>This method provides the unique Firebase Installation ID (FID), which should be used to
   * target this app instance for direct-send messaging.
   *
   * <p>This callback is triggered in the following scenarios:
   *
   * <ul>
   *   <li>When the registration first succeeds after app install (if auto-init is enabled).
   *   <li>When the registration is refreshed due to invalidation or updates (if auto-init is
   *       enabled).
   *   <li>Immediately after a direct call to {@link FirebaseMessaging#register()}.
   * </ul>
   *
   * <p>Ensure the provided `installationId` is uploaded if it hasn't been previously or it might
   * have been deleted on 404s.
   *
   * <p><b>Note:</b> To use this API, you must enable it by adding {@code <meta-data
   * android:name="firebase_messaging_installation_id_enabled" android:value="true" />} to your
   * app's manifest.
   *
   * @param installationId The Firebase Installation ID used for sending messages to the current app
   *     instance.
   */
  @Override
  public void onRegistered(@NonNull String installationId) {
    Log.d(TAG, "Registration: " + installationId);

    // If you want to send messages to this application instance or
    // manage these apps subscriptions on the server side, send the
    // FCM registration to your app server.
    sendRegistrationToServer(installationId);

    // Log and toast
    String msg = getString(R.string.msg_registration_fmt, installationId);
    Log.d(TAG, msg);
    handler.post(() -> Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show());
  }
  // [END onRegistered]

  // [START onUnregistered]
  /**
   * Called when the current app instance has been successfully unregistered from FCM via a call to
   * {@code FirebaseMessaging.unregister()}.
   *
   * <p>This method confirms that the specified FID is no longer active for receiving FCM messages.
   *
   * <p><b>Note:</b> To use this API, you must enable it by adding {@code <meta-data
   * android:name="firebase_messaging_installation_id_enabled" android:value="true" />} to your
   * app's manifest.
   *
   * @param installationId The Firebase Installation ID of the current app instance that was
   *     unregistered with FCM.
   */
  @Override
  public void onUnregistered(@NonNull String installationId) {
    // Remove FCM registration associated with the app instance on the app server
    // so that the app server will not try to send FCM messages to the un registered app instance.
    removeRegistrationFromServer(installationId);
  }

  // [END onUnregistered]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's FCM registration with any
     * server-side account maintained by your application.
     *
     * @param registration The new FCM registration.
     */
    private void sendRegistrationToServer(String registration) {
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
  private void removeRegistrationFromServer(String registration) {
    // TODO: Implement this method to remove FCM registration from your app server.
  }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
