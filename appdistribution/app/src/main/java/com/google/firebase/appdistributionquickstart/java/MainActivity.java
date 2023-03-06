package com.google.firebase.appdistributionquickstart.java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.FirebaseAppDistributionException;
import com.google.firebase.appdistribution.InterruptionLevel;
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppDistribution-Quickstart";

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(
                            MainActivity.this,
                            "The app won't display feedback notifications because the notification permission was denied",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });

    private FirebaseAppDistribution mFirebaseAppDistribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAppDistribution = FirebaseAppDistribution.getInstance();

        binding.btShowNotification.setOnClickListener(view -> {
            mFirebaseAppDistribution.showFeedbackNotification(
                    "Data Collection Notice",
                    InterruptionLevel.HIGH
            );
        });

        binding.btSendFeedback.setOnClickListener(view -> {
            mFirebaseAppDistribution.startFeedback("Thanks for sharing your feedback with us");
        });

        askNotificationPermission();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hide the notification once this activity is destroyed
        mFirebaseAppDistribution.cancelFeedbackNotification();
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // All set. We can post notifications
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Display an educational UI explaining to the user the features that will be enabled
                // by them granting the POST_NOTIFICATION permission. This UI should provide the user
                // "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                // If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
