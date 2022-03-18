package com.google.firebase.appdistributionquickstart.java;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.FirebaseAppDistributionException;
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppDistribution-Quickstart";

    private FirebaseAppDistribution mFirebaseAppDistribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAppDistribution = FirebaseAppDistribution.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAppDistribution.updateIfNewReleaseAvailable()
                .addOnProgressListener(updateProgress -> {
                    // (Optional) Implement custom progress updates in addition to
                    // automatic NotificationManager updates.
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAppDistributionException) {
                        // Handle exception.
                    }
                });
    }
}
