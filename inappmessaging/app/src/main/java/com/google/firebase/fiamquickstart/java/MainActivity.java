package com.google.firebase.fiamquickstart.java;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.fiamquickstart.R;
import com.google.firebase.fiamquickstart.databinding.ActivityMainBinding;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.installations.FirebaseInstallations;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FIAM-Quickstart";

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseInAppMessaging mInAppMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mInAppMessaging = FirebaseInAppMessaging.getInstance();

        mInAppMessaging.setAutomaticDataCollectionEnabled(true);
        mInAppMessaging.setMessagesSuppressed(false);

        binding.eventTriggerButton.setOnClickListener(
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
        FirebaseInstallations.getInstance().getId()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String id) {
                        binding.installationIdText.setText(getString(R.string.installation_id_fmt, id));
                        Log.d(TAG, "Installation id: " + id);
                    }
                });
    }
}
