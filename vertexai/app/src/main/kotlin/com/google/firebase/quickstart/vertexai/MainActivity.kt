/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.vertexai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.vertexai.feature.audio.AudioRoute
import com.google.firebase.quickstart.vertexai.feature.chat.ChatRoute
import com.google.firebase.quickstart.vertexai.feature.functioncalling.FunctionsChatRoute
import com.google.firebase.quickstart.vertexai.feature.image.ImagenRoute
import com.google.firebase.quickstart.vertexai.feature.multimodal.PhotoReasoningRoute
import com.google.firebase.quickstart.vertexai.feature.text.SummarizeRoute
import com.google.firebase.quickstart.vertexai.ui.theme.GenerativeAISample

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GenerativeAISample {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                    ) { innerPadding ->
                        val navController = rememberNavController()

                        NavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = "menu"
                        ) {
                            composable("menu") {
                                MenuScreen(onItemClicked = { routeId ->
                                    navController.navigate(routeId)
                                })
                            }
                            composable("summarize") {
                                SummarizeRoute()
                            }
                            composable("photo_reasoning") {
                                PhotoReasoningRoute()
                            }
                            composable("chat") {
                                ChatRoute()
                            }
                            composable("functions_chat") {
                                FunctionsChatRoute()
                            }
                            composable("audio") {
                                AudioRoute()
                            }
                            composable("images") {
                                ImagenRoute()
                            }
                        }
                    }
                }
            }
        }
    }
}
