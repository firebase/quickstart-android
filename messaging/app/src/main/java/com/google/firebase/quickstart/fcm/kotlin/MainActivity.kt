package com.google.firebase.quickstart.fcm.kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.quickstart.fcm.R
import com.google.firebase.quickstart.fcm.databinding.ActivityMainBinding
import com.google.firebase.quickstart.fcm.kotlin.data.SubscriptionState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: FirebaseMessagingViewModel by viewModels { FirebaseMessagingViewModel.Factory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            viewModel.setNotificationChannel(
                this,
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name)
            )
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        viewModel.getNotificationData(intent)
        // [END handle_data_extras]

        binding.subscribeButton.setOnClickListener {
            // [START subscribe_topics]
            viewModel.getSubscribe("weather")
            // [END subscribe_topics]
        }

        binding.logTokenButton.setOnClickListener {
            // Get token
            // [START log_reg_token]
            viewModel.getToken()
            // [END log_reg_token]
        }

        lifecycleScope.launch {
            viewModel.token.collect { token ->
                if(token.isNotEmpty()){
                    val msg = getString(R.string.msg_token_fmt, token)
                    Snackbar.make(findViewById(android.R.id.content),
                        msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.subscriptionState.collect { state ->
                when (state) {
                    SubscriptionState.Success -> {
                        Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.msg_subscribed), Snackbar.LENGTH_LONG).show()
                    }
                    SubscriptionState.Failed -> { Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.msg_subscribe_failed), Snackbar.LENGTH_LONG).show()
                    }
                    SubscriptionState.Loading -> { }
                }
            }
        }
        Toast.makeText(this, "See README for setup instructions", Toast.LENGTH_SHORT).show()
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
