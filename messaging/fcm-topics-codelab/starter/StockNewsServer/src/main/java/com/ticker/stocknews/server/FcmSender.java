package com.ticker.stocknews.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.InputStream;

public class FcmSender {

  private static InputStream getServiceAccount() {
    return FcmSender.class.getClassLoader().getResourceAsStream("service-account.json");
  }

  private static void initFirebaseSDK() throws Exception {
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(getServiceAccount()))
        .build();

    FirebaseApp.initializeApp(options);
  }

  private static void sendMessageToFcmTopicCondition() throws Exception {
    String topicCondition = ""; // TODO: Provide the Topic Condition you want to send to. Eg., 'Technology' in topics || 'Automotive' in topics

    // TODO: Implement FCM Topic Condition send call

    System.out.printf("Message to FCM Topic Condition %s sent successfully!!", topicCondition);
  }

  private static void sendMessageToFcmTopic() throws Exception {
    String topicName = ""; // TODO: Provide the Topic you want to send to -> /topics/<Topic Name>. Eg., /topics/Technology

    // TODO: Implement FCM Topic send call

    System.out.printf("Message to FCM Topic %s sent successfully!!", topicName);
  }

  private static void sendMessageToFcmRegistrationToken() throws Exception {
    String registrationToken = ""; // TODO: Add FCM Registration Token for target app instance.

    Message message = Message.builder()
        .putData("TUVX", "$50")
        .putData("PQZT", "$100")
        .setNotification(
            Notification.builder()
                .setTitle("Nasdaq up 0.5%")
                .setBody("Markets have opened significantly up from yesterday!!")
                .build())
        .setToken(registrationToken)
        .build();

    FirebaseMessaging.getInstance().send(message);

    System.out.println("Message to FCM Registration Token sent successfully!!");
  }

  public static void main(final String[] args) throws Exception {
    initFirebaseSDK();

    // Note: Enable the call you want to execute. Disable others.
    sendMessageToFcmRegistrationToken();
    // sendMessageToFcmTopic();
    // sendMessageToFcmTopicCondition();
  }
}
