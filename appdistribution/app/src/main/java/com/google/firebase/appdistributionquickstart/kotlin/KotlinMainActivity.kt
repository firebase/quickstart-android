package com.google.firebase.appdistributionquickstart.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.appdistribution.FirebaseAppDistribution
import com.google.firebase.appdistribution.FirebaseAppDistributionException
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var firebaseAppDistribution: FirebaseAppDistribution

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAppDistribution = Firebase.appDistribution
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

    companion object {

        private const val TAG = "AppDistribution-Quickstart"
    }
}
