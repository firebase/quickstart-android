package com.ticker.stocknews;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * The main {@link Application} class
 */
public class StockNewsApplication extends Application {

  private static final String TAG = "StockNewsApplication";

  @Override
  public void onCreate() {
    super.onCreate();
    // Create FCM Registration Token for this app instance
    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        Log.i(TAG, "FCM Registration Token is: " + task.getResult());
        Toast.makeText(StockNewsApplication.this, "FCM Registration Token created. Check logcat.", Toast.LENGTH_LONG)
            .show();
      } else {
        Log.e(TAG, "FCM Registration failed", task.getException());
        Toast.makeText(StockNewsApplication.this, "FCM Registration failed. Check logcat.", Toast.LENGTH_LONG).show();
      }
    });
  }
}
