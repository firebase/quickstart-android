package com.google.firebase.quickstart.database.kotlin.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Navigation
import com.google.firebase.quickstart.database.kotlin.theme.RealtimeDatabaseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RealtimeDatabaseTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

