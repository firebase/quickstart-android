package com.google.firebase.appdistributionquickstart.kotlin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.appdistributionquickstart.databinding.ActivityMainBinding


class KotlinMainActivity : AppCompatActivity() {

    private val appDistributionViewModel: AppDistributionViewModel by viewModels { AppDistributionViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        appDistributionViewModel.updateIfNewRelease()
    }

    companion object {
        private const val TAG = "AppDistribution-Quickstart"
    }
}
