package com.google.firebase.fiamquickstart.kotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.fiamquickstart.R
import com.google.firebase.fiamquickstart.databinding.ActivityMainBinding
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.ktx.Firebase

class KotlinMainActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseIam: FirebaseInAppMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseIam = Firebase.inAppMessaging

        firebaseIam.isAutomaticDataCollectionEnabled = true
        firebaseIam.setMessagesSuppressed(false)

        binding.eventTriggerButton.setOnClickListener { view ->
            firebaseAnalytics.logEvent("engagement_party", Bundle())
            Snackbar.make(view, "'engagement_party' event triggered!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
        }

        // Get and display/log the Instance ID
        FirebaseInstanceId.getInstance().instanceId
                .addOnSuccessListener { instanceIdResult ->
                    val instanceId = instanceIdResult.id
                    binding.instanceIdText.text = getString(R.string.instance_id_fmt, instanceId)
                    Log.d(TAG, "InstanceId: $instanceId")
                }
    }

    companion object {

        private const val TAG = "FIAM-Quickstart"
    }
}
