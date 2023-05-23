package com.google.firebase.fiamquickstart.kotlin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.fiamquickstart.R
import com.google.firebase.fiamquickstart.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class KotlinMainActivity : AppCompatActivity() {

    private val viewModel: InAppMessagingViewModel by viewModels { InAppMessagingViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.eventTriggerButton.setOnClickListener { view ->
            viewModel.triggerEvent()
            Snackbar.make(view, "'engagement_party' event triggered!", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        // Get and display/log the installation id
        lifecycleScope.launch {
            viewModel.installationsId.collect {
                if (it.isNotEmpty()) {
                    binding.installationIdText.text = getString(R.string.installation_id_fmt, it)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FIAM-Quickstart"
    }
}
