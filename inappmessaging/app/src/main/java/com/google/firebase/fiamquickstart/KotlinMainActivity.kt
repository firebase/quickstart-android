package com.google.firebase.fiamquickstart

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.inappmessaging.FirebaseInAppMessaging

class KotlinMainActivity : AppCompatActivity() {

    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var firebaseIam: FirebaseInAppMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseIam = FirebaseInAppMessaging.getInstance()

        firebaseIam.isAutomaticDataCollectionEnabled = true
        firebaseIam.setMessagesSuppressed(false)

        findViewById<View>(R.id.event_trigger_button)
                .setOnClickListener { view ->
                    firebaseAnalytics.logEvent("engagement_party", Bundle())
                    Snackbar.make(view, "'engagement_party' event triggered!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show()
                }

        // Get and display/log the Instance ID
        val textView = findViewById<TextView>(R.id.instance_id_text)
        FirebaseInstanceId.getInstance().instanceId
                .addOnSuccessListener(object : OnSuccessListener<InstanceIdResult> {
                    override fun onSuccess(instanceIdResult: InstanceIdResult) {
                        val instanceId = instanceIdResult.id
                        textView.text = getString(R.string.instance_id_fmt, instanceId)
                        Log.d(TAG, "InstanceId: $instanceId")
                    }
                })
    }

    companion object {

        private val TAG = "FIAM-Quickstart"

    }
}
