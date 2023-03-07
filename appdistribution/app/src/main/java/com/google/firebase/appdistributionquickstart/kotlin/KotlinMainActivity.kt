package com.google.firebase.appdistributionquickstart.kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.FirebaseAppDistributionException
import com.google.firebase.appdistribution.InterruptionLevel
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.appdistributionquickstart.R
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var firebaseAppDistribution: FirebaseAppDistribution

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showFeedbackNotification()
        } else {
            Toast.makeText(
                this@KotlinMainActivity,
                "You won't be able to tap a notification to send feedback because the notification permission was denied",
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
            askNotificationPermission()
        }

        binding.btSendFeedback.setOnClickListener {
            firebaseAppDistribution.startFeedback(R.string.feedback_additional_form_text)
        }
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

    private fun showFeedbackNotification() {
        firebaseAppDistribution.showFeedbackNotification(
            R.string.feedback_additional_form_text,
            InterruptionLevel.HIGH
        )
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // All set. We can post notifications
                showFeedbackNotification()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.i(TAG, "Showing customer rationale for requesting permission.")
                AlertDialog.Builder(this)
                    .setMessage(
                        "Using a notification to initiate feedback to the developer. " +
                                "To enable this feature, allow the app to post notifications."
                    )
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "Launching request for permission.")
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("No thanks") { _, _ -> Log.i(TAG, "User denied permission request.") }
                    .show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            showFeedbackNotification()
        }
    }

    companion object {

        private const val TAG = "KotlinMainActivity.kt"
    }
}
