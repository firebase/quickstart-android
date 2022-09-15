package com.google.samples.quickstart.config

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.samples.quickstart.config.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fetchButton.setOnClickListener {
            viewModel.fetchAndActivateConfig()
        }

        lifecycleScope.launch {
            viewModel.welcomeMessage.collect { message ->
                binding.welcomeTextView.text = message
            }
        }

        lifecycleScope.launch {
            viewModel.allCaps.collect { allCaps ->
                binding.welcomeTextView.isAllCaps = allCaps
            }
        }

        lifecycleScope.launch {
            viewModel.activated.collect { activated ->
                Log.d(TAG, "Config params activated: $activated")
                Toast.makeText(this@MainActivity, "Fetch and activate succeeded",
                    Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchAndActivateConfig()
    }

    companion object {

        private const val TAG = "MainActivity"
    }
}
