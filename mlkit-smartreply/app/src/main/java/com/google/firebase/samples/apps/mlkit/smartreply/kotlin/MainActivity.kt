package com.google.firebase.samples.apps.mlkit.smartreply.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.google.firebase.samples.apps.mlkit.smartreply.R
import com.google.firebase.samples.apps.mlkit.smartreply.kotlin.chat.ChatFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ChatFragment.newInstance())
                    .commitNow()
        }
    }
}
