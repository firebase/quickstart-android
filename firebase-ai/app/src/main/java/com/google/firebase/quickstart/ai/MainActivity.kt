package com.google.firebase.quickstart.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.ai.feature.live.BidiViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenViewModel
import com.google.firebase.quickstart.ai.feature.text.ChatViewModel
import com.google.firebase.quickstart.ai.ui.ChatScreen
import com.google.firebase.quickstart.ai.ui.ImagenScreen
import com.google.firebase.quickstart.ai.ui.ServerPromptScreen
import com.google.firebase.quickstart.ai.ui.StreamRealtimeScreen
import com.google.firebase.quickstart.ai.ui.StreamRealtimeVideoScreen
import com.google.firebase.quickstart.ai.ui.SvgScreen
import com.google.firebase.quickstart.ai.ui.navigation.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.ui.navigation.MainMenuScreen
import com.google.firebase.quickstart.ai.ui.navigation.ScreenType
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAILogicTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        catImage = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cat)
        setContent {
            val navController = rememberNavController()

            var topBarTitle: String by rememberSaveable { mutableStateOf(getString(R.string.app_name)) }
            FirebaseAILogicTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            title = {
                                Text(topBarTitle)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = "mainMenu",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("mainMenu") {
                            MainMenuScreen(
                                onSampleClicked = {
                                    navController.navigate(it.route)
                                }
                            )
                        }

                        // Add navigation for all of the samples
                        FIREBASE_AI_SAMPLES.forEach { sample ->
                            composable(
                                route = sample.route::class,
                                typeMap = emptyMap()
                            ) {
                                when (sample.screenType) {
                                    ScreenType.CHAT -> {
                                        val viewModel: ChatViewModel = viewModel(
                                            modelClass = sample.viewModelClass!!.java
                                        ) as ChatViewModel
                                        ChatScreen(viewModel)
                                    }

                                    ScreenType.IMAGEN -> {
                                        val viewModel: ImagenViewModel = viewModel(
                                            modelClass = sample.viewModelClass!!.java
                                        ) as ImagenViewModel
                                        ImagenScreen(viewModel)
                                    }

                                    ScreenType.SVG -> {
                                        SvgScreen()
                                    }

                                    ScreenType.SERVER_PROMPT -> {
                                        ServerPromptScreen()
                                    }

                                    ScreenType.BIDI -> {
                                        val viewModel: BidiViewModel = viewModel(
                                            modelClass = sample.viewModelClass!!.java
                                        ) as BidiViewModel
                                        @SuppressLint("MissingPermission")
                                        StreamRealtimeScreen(viewModel)
                                    }

                                    ScreenType.BIDI_VIDEO -> {
                                        val viewModel: BidiViewModel = viewModel(
                                            modelClass = sample.viewModelClass!!.java
                                        ) as BidiViewModel
                                        @SuppressLint("MissingPermission")
                                        StreamRealtimeVideoScreen(viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    if (destination.route == "mainMenu") {
                        topBarTitle = getString(R.string.app_name)
                    }
                }
            })
        }
    }

    companion object {
        lateinit var catImage: Bitmap
    }
}
