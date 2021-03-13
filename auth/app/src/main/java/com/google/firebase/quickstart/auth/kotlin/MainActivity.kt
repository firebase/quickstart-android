package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.quickstart.auth.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph_kotlin)
    }
}