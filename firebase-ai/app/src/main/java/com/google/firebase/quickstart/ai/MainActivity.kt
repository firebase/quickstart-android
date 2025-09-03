package com.google.firebase.quickstart.ai

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeRoute
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeScreen
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenScreen
import com.google.firebase.quickstart.ai.feature.text.ChatRoute
import com.google.firebase.quickstart.ai.feature.text.ChatScreen
import com.google.firebase.quickstart.ai.ui.navigation.MainMenuScreen
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAILogicTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
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
                                titleContentColor = MaterialTheme.colorScheme.primary
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
                                    topBarTitle = it.title
                                    when (it.navRoute) {
                                        "chat" -> {
                                            navController.navigate(ChatRoute(it.id))
                                        }

                                        "imagen" -> {
                                            navController.navigate(ImagenRoute(it.id))
                                        }

                                        "stream" -> {
                                            navController.navigate(StreamRealtimeRoute(it.id))
                                        }
                                    }
                                }
                            )
                        }
                        // Text Samples
                        composable<ChatRoute> {
                            ChatScreen()
                        }
                        // Imagen Samples
                        composable<ImagenRoute> {
                            ImagenScreen()
                        }
                        // Stream Realtime Samples
                        composable<StreamRealtimeRoute> {
                            StreamRealtimeScreen()
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
    companion object{
        lateinit var catImage: Bitmap
    }
}
