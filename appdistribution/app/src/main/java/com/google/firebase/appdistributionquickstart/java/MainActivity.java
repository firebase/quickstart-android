package com.google.firebase.appdistributionquickstart.java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.FirebaseAppDistributionException;
import com.google.firebase.appdistribution.InterruptionLevel;
import com.google.firebase.appdistributionquickstart.R;
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity.java";

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showFeedbackNotification();
                } else {
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
            askNotificationPermission();
        });

        binding.btSendFeedback.setOnClickListener(view -> {
            mFirebaseAppDistribution.startFeedback(R.string.feedbackAdditionalFormText);
        });
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

    private void showFeedbackNotification() {
        mFirebaseAppDistribution.showFeedbackNotification(
                R.string.feedbackAdditionalFormText,
                InterruptionLevel.HIGH
        );
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // All set. We can post notifications
                showFeedbackNotification();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.i(TAG, "Showing customer rationale for requesting permission.");
                new AlertDialog.Builder(this)
                        .setMessage("Using a notification to initiate feedback to the developer. " +
                                "To enable this feature, allow the app to post notifications."
                        )
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            Log.i(TAG, "Launching request for permission.");
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        })
                        .setNegativeButton("No thanks", (dialogInterface, i) -> Log.i(TAG, "User denied permission request."))
                        .show();
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
