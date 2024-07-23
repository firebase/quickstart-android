package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { Navigate() }
    }
}

@Composable
fun Navigate() {
    Navigation()
}