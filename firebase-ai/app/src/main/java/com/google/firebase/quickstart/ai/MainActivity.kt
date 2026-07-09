package com.google.firebase.quickstart.ai

import kotlin.reflect.KClass
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
import com.google.firebase.quickstart.ai.feature.hybrid.HybridInferenceRoute
import com.google.firebase.quickstart.ai.feature.hybrid.HybridInferenceViewModel
import com.google.firebase.quickstart.ai.feature.structured.StructuredOutputRoute
import com.google.firebase.quickstart.ai.feature.structured.StructuredOutputViewModel
import com.google.firebase.quickstart.ai.feature.live.StreamAudioViewModel
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeAudioRoute
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeVideoRoute
import com.google.firebase.quickstart.ai.feature.live.StreamVideoViewModel
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationViewModel
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationViewModel
import com.google.firebase.quickstart.ai.feature.text.AutoFunctionCallRoute
import com.google.firebase.quickstart.ai.feature.text.AutoFunctionCallViewModel
import com.google.firebase.quickstart.ai.feature.text.ChatViewModel
import com.google.firebase.quickstart.ai.feature.text.DocumentComparisonRoute
import com.google.firebase.quickstart.ai.feature.text.DocumentComparisonViewModel
import com.google.firebase.quickstart.ai.feature.text.GoogleSearchGroundingRoute
import com.google.firebase.quickstart.ai.feature.text.GoogleSearchGroundingViewModel
import com.google.firebase.quickstart.ai.feature.text.ImageBlogCreatorRoute
import com.google.firebase.quickstart.ai.feature.text.ImageBlogCreatorViewModel
import com.google.firebase.quickstart.ai.feature.text.NanoBanana2Route
import com.google.firebase.quickstart.ai.feature.text.NanoBanana2ViewModel
import com.google.firebase.quickstart.ai.feature.text.NanoBananaLiteRoute
import com.google.firebase.quickstart.ai.feature.text.NanoBananaLiteViewModel
import com.google.firebase.quickstart.ai.feature.text.NanoBananaProRoute
import com.google.firebase.quickstart.ai.feature.text.NanoBananaProViewModel
import com.google.firebase.quickstart.ai.feature.text.NanoBananaRoute
import com.google.firebase.quickstart.ai.feature.text.NanoBananaViewModel
import com.google.firebase.quickstart.ai.feature.text.ServerPromptTemplateRoute
import com.google.firebase.quickstart.ai.feature.text.ServerPromptTemplateViewModel
import com.google.firebase.quickstart.ai.feature.text.SvgRoute
import com.google.firebase.quickstart.ai.feature.text.SvgViewModel
import com.google.firebase.quickstart.ai.feature.text.TranslationRoute
import com.google.firebase.quickstart.ai.feature.text.TranslationViewModel
import com.google.firebase.quickstart.ai.feature.text.VideoHashtagGeneratorRoute
import com.google.firebase.quickstart.ai.feature.text.VideoHashtagGeneratorViewModel
import com.google.firebase.quickstart.ai.feature.text.VideoSummarizationRoute
import com.google.firebase.quickstart.ai.feature.text.VideoSummarizationViewModel
import com.google.firebase.quickstart.ai.feature.text.WeatherChatRoute
import com.google.firebase.quickstart.ai.feature.text.WeatherChatViewModel
import com.google.firebase.quickstart.ai.ui.ChatScreen
import com.google.firebase.quickstart.ai.ui.HybridInferenceScreen
import com.google.firebase.quickstart.ai.ui.ServerPromptScreen
import com.google.firebase.quickstart.ai.ui.StreamRealtimeScreen
import com.google.firebase.quickstart.ai.ui.StreamRealtimeVideoScreen
import com.google.firebase.quickstart.ai.ui.StructuredOutputScreen
import com.google.firebase.quickstart.ai.ui.SvgScreen
import com.google.firebase.quickstart.ai.ui.navigation.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.ui.navigation.MainMenuScreen
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
                                    topBarTitle = it.title
                                    navController.navigate(it.route)
                                }
                            )
                        }

                        // Add navigation for all of the samples
                        composable<TranslationRoute> { ChatScreen(viewModel<TranslationViewModel>()) }
                        composable<SvgRoute> { SvgScreen(viewModel<SvgViewModel>()) }
                        composable<AudioSummarizationRoute> { ChatScreen(viewModel<AudioSummarizationViewModel>()) }
                        composable<VideoSummarizationRoute> { ChatScreen(viewModel<VideoSummarizationViewModel>()) }
                        composable<AudioTranslationRoute> { ChatScreen(viewModel<AudioTranslationViewModel>()) }
                        composable<ImageBlogCreatorRoute> { ChatScreen(viewModel<ImageBlogCreatorViewModel>()) }
                        composable<NanoBananaLiteRoute> { ChatScreen(viewModel<NanoBananaLiteViewModel>()) }
                        composable<NanoBanana2Route> { ChatScreen(viewModel<NanoBanana2ViewModel>()) }
                        composable<NanoBananaProRoute> { ChatScreen(viewModel<NanoBananaProViewModel>()) }
                        composable<NanoBananaRoute> { ChatScreen(viewModel<NanoBananaViewModel>()) }
                        composable<DocumentComparisonRoute> { ChatScreen(viewModel<DocumentComparisonViewModel>()) }
                        composable<VideoHashtagGeneratorRoute> { ChatScreen(viewModel<VideoHashtagGeneratorViewModel>()) }
                        composable<GoogleSearchGroundingRoute> { ChatScreen(viewModel<GoogleSearchGroundingViewModel>()) }
                        composable<WeatherChatRoute> { ChatScreen(viewModel<WeatherChatViewModel>()) }
                        composable<AutoFunctionCallRoute> { ChatScreen(viewModel<AutoFunctionCallViewModel>()) }
                        composable<StreamRealtimeAudioRoute> {
                            @SuppressLint("MissingPermission")
                            StreamRealtimeScreen(viewModel<StreamAudioViewModel>())
                        }
                        composable<StreamRealtimeVideoRoute> {
                            @SuppressLint("MissingPermission")
                            StreamRealtimeVideoScreen(viewModel<StreamVideoViewModel>())
                        }
                        composable<ServerPromptTemplateRoute> { ServerPromptScreen(viewModel<ServerPromptTemplateViewModel>()) }
                        composable<HybridInferenceRoute> { HybridInferenceScreen(viewModel<HybridInferenceViewModel>()) }
                        composable<StructuredOutputRoute> { StructuredOutputScreen(viewModel<StructuredOutputViewModel>()) }
                    }
                }
            }
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.route == "mainMenu") {
                    topBarTitle = getString(R.string.app_name)
                }
            }
        }
    }

    companion object {
        lateinit var catImage: Bitmap
    }
}
