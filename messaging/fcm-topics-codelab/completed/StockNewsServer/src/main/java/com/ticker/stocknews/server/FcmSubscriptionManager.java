package com.ticker.stocknews.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.TopicManagementResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class FcmSubscriptionManager {

  private static InputStream getServiceAccount() {
    return FcmSender.class.getClassLoader().getResourceAsStream("service-account.json");
  }

  private static void initFirebaseSDK() throws Exception {
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(getServiceAccount()))
        .build();

    FirebaseApp.initializeApp(options);
  }

  private static void subscribeFcmRegistrationTokensToTopic() throws Exception {
    List<String> registrationTokens = Arrays
        .asList("REPLACE_WITH_FCM_REGISTRATION_TOKEN"); // TODO: add FCM Registration Tokens to subscribe
    String topicName = "/topics/Energy";

    TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
        registrationTokens, topicName);
    System.out.printf("Num tokens successfully subscribed %d", response.getSuccessCount());
  }

  private static void unsubscribeFcmRegistrationTokensFromTopic() throws Exception {
    List<String> registrationTokens = Arrays
        .asList("REPLACE_WITH_FCM_REGISTRATION_TOKEN"); // TODO: add FCM Registration Tokens to unsubscribe
    String topicName = "/topics/Energy";

    TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
        registrationTokens, topicName);
    System.out.printf("Num tokens successfully unsubscribed %d", response.getSuccessCount());
  }

  public static void main(final String[] args) throws Exception {
    initFirebaseSDK();

    // Note: Enable the call you want to execute. Disable others.
    subscribeFcmRegistrationTokensToTopic();
    // unsubscribeFcmRegistrationTokensFromTopic();
  }
}
