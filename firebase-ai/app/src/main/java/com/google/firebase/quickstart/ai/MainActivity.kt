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
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeRoute
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeScreen
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeVideoRoute
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeVideoScreen
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenGenerationRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenGenerationViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenInpaintingRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenInpaintingViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenOutpaintingRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenOutpaintingViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenScreen
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenStyleTransferRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenStyleTransferViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenSubjectReferenceRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenSubjectReferenceViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenTemplateRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenTemplateViewModel
import com.google.firebase.quickstart.ai.feature.svg.SvgRoute
import com.google.firebase.quickstart.ai.feature.svg.SvgScreen
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationViewModel
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationViewModel
import com.google.firebase.quickstart.ai.feature.text.ChatRoute
import com.google.firebase.quickstart.ai.feature.text.ChatScreen
import com.google.firebase.quickstart.ai.feature.text.CourseRecommendationsRoute
import com.google.firebase.quickstart.ai.feature.text.CourseRecommendationsViewModel
import com.google.firebase.quickstart.ai.feature.text.DocumentComparisonRoute
import com.google.firebase.quickstart.ai.feature.text.DocumentComparisonViewModel
import com.google.firebase.quickstart.ai.feature.text.GoogleSearchGroundingRoute
import com.google.firebase.quickstart.ai.feature.text.GoogleSearchGroundingViewModel
import com.google.firebase.quickstart.ai.feature.text.ImageBlogCreatorRoute
import com.google.firebase.quickstart.ai.feature.text.ImageBlogCreatorViewModel
import com.google.firebase.quickstart.ai.feature.text.ImageGenerationRoute
import com.google.firebase.quickstart.ai.feature.text.ImageGenerationViewModel
import com.google.firebase.quickstart.ai.feature.text.LegacyChatViewModel
import com.google.firebase.quickstart.ai.feature.text.TextGenRoute
import com.google.firebase.quickstart.ai.feature.text.TextGenScreen
import com.google.firebase.quickstart.ai.feature.text.ThinkingChatRoute
import com.google.firebase.quickstart.ai.feature.text.ThinkingChatViewModel
import com.google.firebase.quickstart.ai.feature.text.TravelTipsRoute
import com.google.firebase.quickstart.ai.feature.text.TravelTipsViewModel
import com.google.firebase.quickstart.ai.feature.text.VideoHashtagGeneratorRoute
import com.google.firebase.quickstart.ai.feature.text.VideoHashtagGeneratorViewModel
import com.google.firebase.quickstart.ai.feature.text.VideoSummarizationRoute
import com.google.firebase.quickstart.ai.feature.text.VideoSummarizationViewModel
import com.google.firebase.quickstart.ai.feature.text.WeatherChatRoute
import com.google.firebase.quickstart.ai.feature.text.WeatherChatViewModel
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
                                    when (it.title) {
                                        "Travel tips" -> navController.navigate(TravelTipsRoute)
                                        "Weather Chat" -> navController.navigate(WeatherChatRoute)
                                        "Chatbot recommendations for courses" -> navController.navigate(CourseRecommendationsRoute)
                                        "Audio Summarization" -> navController.navigate(AudioSummarizationRoute)
                                        "Translation from audio (Vertex AI)" -> navController.navigate(AudioTranslationRoute)
                                        "Blog post creator (Vertex AI)" -> navController.navigate(ImageBlogCreatorRoute)
                                        "Gemini 2.5 Flash Image (aka nanobanana)" -> navController.navigate(ImageGenerationRoute)
                                        "Document comparison (Vertex AI)" -> navController.navigate(DocumentComparisonRoute)
                                        "Hashtags for a video (Vertex AI)" -> navController.navigate(VideoHashtagGeneratorRoute)
                                        "Summarize video" -> navController.navigate(VideoSummarizationRoute)
                                        "Grounding with Google Search" -> navController.navigate(GoogleSearchGroundingRoute)
                                        "Thinking" -> navController.navigate(ThinkingChatRoute)
                                        "Imagen 4 - image generation" -> navController.navigate(ImagenGenerationRoute)
                                        "Imagen 3 - Inpainting (Vertex AI)" -> navController.navigate(ImagenInpaintingRoute)
                                        "Imagen 3 - Outpainting (Vertex AI)" -> navController.navigate(ImagenOutpaintingRoute)
                                        "Imagen 3 - Subject Reference (Vertex AI)" -> navController.navigate(ImagenSubjectReferenceRoute)
                                        "Imagen 3 - Style Transfer (Vertex AI)" -> navController.navigate(ImagenStyleTransferRoute)
                                        "Server Prompt Template - Imagen" -> navController.navigate(ImagenTemplateRoute)
                                        else -> {
                                            when (it.navRoute) {
                                                "chat" -> navController.navigate(ChatRoute(it.id))
                                                "stream" -> navController.navigate(StreamRealtimeRoute(it.id))
                                                "streamVideo" -> navController.navigate(StreamRealtimeVideoRoute(it.id))
                                                "text" -> navController.navigate(TextGenRoute(it.id))
                                                "svg" -> navController.navigate(SvgRoute(it.id))
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        // Refactored Chat Samples
                        composable<TravelTipsRoute> {
                            val viewModel: TravelTipsViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<WeatherChatRoute> {
                            val viewModel: WeatherChatViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<CourseRecommendationsRoute> {
                            val viewModel: CourseRecommendationsViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<AudioSummarizationRoute> {
                            val viewModel: AudioSummarizationViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<AudioTranslationRoute> {
                            val viewModel: AudioTranslationViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<ImageBlogCreatorRoute> {
                            val viewModel: ImageBlogCreatorViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<ImageGenerationRoute> {
                            val viewModel: ImageGenerationViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<DocumentComparisonRoute> {
                            val viewModel: DocumentComparisonViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<VideoHashtagGeneratorRoute> {
                            val viewModel: VideoHashtagGeneratorViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<VideoSummarizationRoute> {
                            val viewModel: VideoSummarizationViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<GoogleSearchGroundingRoute> {
                            val viewModel: GoogleSearchGroundingViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        composable<ThinkingChatRoute> {
                            val viewModel: ThinkingChatViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        // Generic Chat Samples (Legacy)
                        composable<ChatRoute> {
                            val viewModel: LegacyChatViewModel = viewModel()
                            ChatScreen(viewModel)
                        }
                        // Refactored Imagen Samples
                        composable<ImagenGenerationRoute> {
                            val viewModel: ImagenGenerationViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        composable<ImagenInpaintingRoute> {
                            val viewModel: ImagenInpaintingViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        composable<ImagenOutpaintingRoute> {
                            val viewModel: ImagenOutpaintingViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        composable<ImagenSubjectReferenceRoute> {
                            val viewModel: ImagenSubjectReferenceViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        composable<ImagenStyleTransferRoute> {
                            val viewModel: ImagenStyleTransferViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        composable<ImagenTemplateRoute> {
                            val viewModel: ImagenTemplateViewModel = viewModel()
                            ImagenScreen(viewModel)
                        }
                        // Final verification
                        // The permission is checked by the @RequiresPermission annotation on the
                        // StreamRealtimeScreen composable.
                        @SuppressLint("MissingPermission")
                        composable<StreamRealtimeRoute> {
                            StreamRealtimeScreen()
                        }
                        // The permission is checked by the @RequiresPermission annotation on the
                        // StreamRealtimeVideoScreen composable.
                        @SuppressLint("MissingPermission")
                        composable<StreamRealtimeVideoRoute> {
                            StreamRealtimeVideoScreen()
                        }
                        composable<TextGenRoute> {
                            TextGenScreen()
                        }
                        composable<SvgRoute> {
                            SvgScreen()
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
