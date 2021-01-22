package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.databinding.ActivityMain2Binding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val fab = binding.fab
        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.nav_graph_kotlin)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.SignInFragment, R.id.NewPostFragment, R.id.PostDetailFragment -> {
                    fab.isGone = true
                }
                R.id.MainFragment -> {
                    fab.isVisible = true
                    fab.setOnClickListener {
                        navController.navigate(R.id.action_MainFragment_to_NewPostFragment)
                    }
                }
            }
        }
    }
}