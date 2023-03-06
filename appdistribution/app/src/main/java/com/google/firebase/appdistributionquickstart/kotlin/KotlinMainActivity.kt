package com.google.firebase.appdistributionquickstart.kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.FirebaseAppDistributionException
import com.google.firebase.appdistribution.InterruptionLevel
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var firebaseAppDistribution: FirebaseAppDistribution

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                this@KotlinMainActivity,
                "The app won't display feedback notifications because the notification permission was denied",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAppDistribution = Firebase.appDistribution

        binding.btShowNotification.setOnClickListener {
            firebaseAppDistribution.showFeedbackNotification(
                "Data Collection Notice",
                InterruptionLevel.HIGH
            )
        }

        binding.btSendFeedback.setOnClickListener {
            firebaseAppDistribution.startFeedback("Thanks for sharing your feedback with us")
        }

        askNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        firebaseAppDistribution.updateIfNewReleaseAvailable()
            .addOnProgressListener { updateProgress ->
                // (Optional) Implement custom progress updates in addition to
                // automatic NotificationManager updates.
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAppDistributionException) {
                    // Handle exception.
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hide the notification once this activity is destroyed
        firebaseAppDistribution.cancelFeedbackNotification()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // All set. We can post notifications
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Display an educational UI explaining to the user the features that will be enabled
                // by them granting the POST_NOTIFICATION permission. This UI should provide the user
                // "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                // If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {

        private const val TAG = "AppDistribution-Quickstart"
    }
}
