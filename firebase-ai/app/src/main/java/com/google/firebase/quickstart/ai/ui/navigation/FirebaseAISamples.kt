package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.quickstart.ai.feature.live.StreamAudioViewModel
import com.google.firebase.quickstart.ai.feature.live.StreamVideoViewModel
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeAudioRoute
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeVideoRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenGenerationRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenGenerationViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenInpaintingRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenInpaintingViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenOutpaintingRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenOutpaintingViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenStyleTransferRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenStyleTransferViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenSubjectReferenceRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenSubjectReferenceViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenTemplateRoute
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenTemplateViewModel
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioSummarizationViewModel
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationRoute
import com.google.firebase.quickstart.ai.feature.text.AudioTranslationViewModel
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
import com.google.firebase.quickstart.ai.feature.text.ServerPromptTemplateRoute
import com.google.firebase.quickstart.ai.feature.text.ServerPromptTemplateViewModel
import com.google.firebase.quickstart.ai.feature.text.SvgRoute
import com.google.firebase.quickstart.ai.feature.text.SvgViewModel
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

val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        title = "Travel tips",
        description = "The user wants the model to help a new traveler" +
                " with travel tips",
        route = TravelTipsRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = TravelTipsViewModel::class,
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Chatbot recommendations for courses",
        description = "A chatbot suggests courses for a performing arts program.",
        route = CourseRecommendationsRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = CourseRecommendationsViewModel::class,
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Audio Summarization",
        description = "Summarize an audio file",
        route = AudioSummarizationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = AudioSummarizationViewModel::class,
        categories = listOf(Category.AUDIO),
    ),
    Sample(
        title = "Translation from audio (Vertex AI)",
        description = "Translate an audio file stored in Cloud Storage",
        route = AudioTranslationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = AudioTranslationViewModel::class,
        categories = listOf(Category.AUDIO)
    ),
    Sample(
        title = "Blog post creator (Vertex AI)",
        description = "Create a blog post from an image file stored in Cloud Storage.",
        route = ImageBlogCreatorRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = ImageBlogCreatorViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 4 - image generation",
        description = "Generate images using Imagen 4",
        route = ImagenGenerationRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenGenerationViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Inpainting (Vertex AI)",
        description = "Replace part of an image using Imagen 3",
        route = ImagenInpaintingRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenInpaintingViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Outpainting (Vertex AI)",
        description = "Expand an image by drawing in more background",
        route = ImagenOutpaintingRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenOutpaintingViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Subject Reference (Vertex AI)",
        description = "Generate an image using a referenced subject (must be an animal)",
        route = ImagenSubjectReferenceRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenSubjectReferenceViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Style Transfer (Vertex AI)",
        description = "Change the art style of a cat picture using a reference",
        route = ImagenStyleTransferRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenStyleTransferViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Gemini 2.5 Flash Image (aka nanobanana)",
        description = "Generate and/or edit images using Gemini 2.5 Flash Image aka nanobanana",
        route = ImageGenerationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = ImageGenerationViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Document comparison (Vertex AI)",
        description = "Compare the contents of 2 documents." +
                " Only supported by the Vertex AI Gemini API because the documents are stored in Cloud Storage",
        route = DocumentComparisonRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = DocumentComparisonViewModel::class,
        categories = listOf(Category.DOCUMENT)
    ),
    Sample(
        title = "Hashtags for a video (Vertex AI)",
        description = "Generate hashtags for a video ad stored in Cloud Storage",
        route = VideoHashtagGeneratorRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = VideoHashtagGeneratorViewModel::class,
        categories = listOf(Category.VIDEO)
    ),
    Sample(
        title = "Summarize video",
        description = "Summarize a video and extract important dialogue.",
        route = VideoSummarizationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = VideoSummarizationViewModel::class,
        categories = listOf(Category.VIDEO)
    ),
    Sample(
        title = "ForecastTalk",
        description = "Use bidirectional streaming to get information about" +
                " weather conditions for a specific US city on a specific date",
        route = StreamRealtimeAudioRoute,
        screenType = ScreenType.BIDI,
        viewModelClass = StreamAudioViewModel::class,
        categories = listOf(Category.LIVE_API, Category.AUDIO, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Gemini Live (Video input)",
        description = "Use bidirectional streaming to chat with Gemini using your" +
                " phone's camera",
        route = StreamRealtimeVideoRoute,
        screenType = ScreenType.BIDI_VIDEO,
        viewModelClass = StreamVideoViewModel::class,
        categories = listOf(Category.LIVE_API, Category.VIDEO, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Weather Chat",
        description = "Use function calling to get the weather conditions" +
                " for a specific US city on a specific date.",
        route = WeatherChatRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = WeatherChatViewModel::class,
        categories = listOf(Category.TEXT, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Grounding with Google Search",
        description = "Use Grounding with Google Search to get responses based on up-to-date information from the" +
                " web.",
        route = GoogleSearchGroundingRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = GoogleSearchGroundingViewModel::class,
        categories = listOf(Category.TEXT, Category.DOCUMENT)
    ),
    Sample(
        title = "Server Prompt Template - Imagen",
        description = "Generate an image using a server prompt template. Note that you need to setup the template in " +
                "the Firebase console before running this demo.",
        route = ImagenTemplateRoute,
        screenType = ScreenType.IMAGEN,
        viewModelClass = ImagenTemplateViewModel::class,
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Server Prompt Templates - Gemini",
        description = "Generate an invoice using server prompt templates.  Note that you need to setup the template" +
                " in the Firebase console before running this demo.",
        route = ServerPromptTemplateRoute,
        screenType = ScreenType.SERVER_PROMPT,
        viewModelClass = ServerPromptTemplateViewModel::class,
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Thinking",
        description = "Gemini 2.5 Flash with dynamic thinking",
        route = ThinkingChatRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = ThinkingChatViewModel::class,
        categories = listOf(Category.TEXT)
    ),
    Sample(
        title = "SVG Generator",
        description = "Use Gemini 3 Flash preview to create SVG illustrations",
        route = SvgRoute,
        screenType = ScreenType.SVG,
        viewModelClass = SvgViewModel::class,
        categories = listOf(Category.IMAGE, Category.TEXT)
    )
)
