package com.google.firebase.quickstart.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.ai.feature.live.StreamSample
import com.google.firebase.quickstart.ai.feature.media.MediaSamples
import com.google.firebase.quickstart.ai.feature.text.ChatScreen
import com.google.firebase.quickstart.ai.feature.text.TextSamples
import com.google.firebase.quickstart.ai.navigation.TextSample
import com.google.firebase.quickstart.ai.ui.navigation.MainMenuScreen
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAIServicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FirebaseAIServicesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = "mainMenu",
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("mainMenu") {
                            MainMenuScreen(
                                onTextSampleClicked = {
                                    navController.navigate(TextSample("hey"))
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Text Samples
                        composable<TextSample> {
                            ChatScreen()
                        }
                    }
                }
            }
        }
    }
}
