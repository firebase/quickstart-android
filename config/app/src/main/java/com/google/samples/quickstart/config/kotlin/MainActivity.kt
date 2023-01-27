package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.samples.quickstart.config.R
import com.google.samples.quickstart.config.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: RemoteConfigViewModel by viewModels { RemoteConfigViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fetchButton.setOnClickListener { viewModel.fetchRemoteConfig() }

        viewModel.enableDeveloperMode()

        viewModel.setDefaultValues(R.xml.remote_config_defaults)

        lifecycleScope.launch {
            viewModel.welcomeMessage.collect { welcomeMessage ->
                binding.welcomeTextView.text = welcomeMessage
            }
        }
    }
}
