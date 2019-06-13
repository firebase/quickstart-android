package com.google.firebase.fiamquickstart.java;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.fiamquickstart.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "FIAM-Quickstart";

  private FirebaseAnalytics mFirebaseAnalytics;
  private FirebaseInAppMessaging mInAppMessaging;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    mInAppMessaging = FirebaseInAppMessaging.getInstance();

    mInAppMessaging.setAutomaticDataCollectionEnabled(true);
    mInAppMessaging.setMessagesSuppressed(false);

    findViewById(R.id.eventTriggerButton)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                mFirebaseAnalytics.logEvent("engagement_party", new Bundle());
                Snackbar.make(view, "'engagement_party' event triggered!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
              }
            });

    // Get and display/log the Instance ID
    final TextView textView = findViewById(R.id.instanceIdText);
    FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
              @Override
              public void onSuccess(InstanceIdResult instanceIdResult) {
                String instanceId = instanceIdResult.getId();
                textView.setText(getString(R.string.instance_id_fmt, instanceId));
                Log.d(TAG, "InstanceId: " + instanceId);
              }
            });
  }
}
