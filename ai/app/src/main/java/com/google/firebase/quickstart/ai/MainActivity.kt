package com.google.firebase.quickstart.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.ai.feature.text.ChatScreen
import com.google.firebase.quickstart.ai.ui.navigation.TextSample
import com.google.firebase.quickstart.ai.ui.navigation.MainMenuScreen
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAILogicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FirebaseAILogicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = "mainMenu",
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("mainMenu") {
                            MainMenuScreen(
                                onSampleClicked = {
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
