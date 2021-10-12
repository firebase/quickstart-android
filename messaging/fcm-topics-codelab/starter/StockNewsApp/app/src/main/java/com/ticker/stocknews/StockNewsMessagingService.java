package com.ticker.stocknews;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Handles messages that arrive from Firebase Cloud Messaging
 */
public class StockNewsMessagingService extends FirebaseMessagingService {

  private static final String TAG = "StockNewsMessagingService";

  /**
   * This method gets called whenever a new FCM Token is generated for an app instance. For more details refer: {@link
   * <a href = "https://firebase.google.com/docs/cloud-messaging/android/client#monitor-token-generation" />}.
   */
  @Override
  public void onNewToken(String token) {
    Log.i(TAG, "FCM Registration Token created or refreshed: " + token);
  }

  /**
   * This method will be called when the app is in the foreground and it received a message from Firebase Cloud
   * Messaging. To learn more about the differences between receiving messages in foreground and background refer:
   * {@link <a href = "https://firebase.google.com/docs/cloud-messaging/android/receive#handling_messages" />}
   */
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    String logMessage = (remoteMessage.getNotification() == null) ?
        String.format("Notification received with data-payload %s", remoteMessage.getData()) :
        String.format("Notification received with data-payload %s, title %s, body %s", remoteMessage.getData(),
            remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    Log.i(TAG, logMessage);
  }
}
