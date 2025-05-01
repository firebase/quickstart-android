package com.google.firebase.quickstart.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.quickstart.ai.navigation.AppDestinations
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAIServicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseAIServicesTheme {
                var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.CHAT) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationSuiteScaffold(
                        navigationSuiteItems = {
                            AppDestinations.entries.forEach {
                                item(
                                    icon = {
                                        Icon(
                                            it.icon,
                                            contentDescription = it.label
                                        )
                                    },
                                    label = { Text(it.label) },
                                    selected = it == currentDestination,
                                    onClick = { currentDestination = it }
                                )
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // TODO: Destinations
                        LandingScreen()
                    }
                }
            }
        }
    }
}
