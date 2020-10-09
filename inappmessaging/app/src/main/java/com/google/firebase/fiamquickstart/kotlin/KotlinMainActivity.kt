package com.google.firebase.fiamquickstart.kotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.fiamquickstart.R
import com.google.firebase.fiamquickstart.databinding.ActivityMainBinding
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.installations.ktx.installations
import com.google.firebase.ktx.Firebase

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseIam: FirebaseInAppMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics
        firebaseIam = Firebase.inAppMessaging

        firebaseIam.isAutomaticDataCollectionEnabled = true
        firebaseIam.setMessagesSuppressed(false)

        binding.eventTriggerButton.setOnClickListener { view ->
            firebaseAnalytics.logEvent("engagement_party", Bundle())
            Snackbar.make(view, "'engagement_party' event triggered!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
        }

        // Get and display/log the installation id
        Firebase.installations.getId()
                .addOnSuccessListener { id ->
                    binding.installationIdText.text = getString(R.string.installation_id_fmt, id)
                    Log.d(TAG, "Installation ID: $id")
                }
    }

    companion object {

        private const val TAG = "FIAM-Quickstart"
    }
}
