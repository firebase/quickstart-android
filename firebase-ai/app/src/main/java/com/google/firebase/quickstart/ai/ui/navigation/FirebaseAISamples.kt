package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.quickstart.ai.feature.hybrid.HybridInferenceRoute
import com.google.firebase.quickstart.ai.feature.hybrid.HybridInferenceViewModel
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

val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        title = "Translate text",
        description = "Use Gemini 3.5 Flash-Lite to translate text",
        route = TranslationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = TranslationViewModel::class,
        categories = listOf(Category.GEMINI3)
    ),
    Sample(
        title = "SVG Generator",
        description = "Use Gemini 3.6 Flash to create SVG illustrations",
        route = SvgRoute,
        screenType = ScreenType.SVG,
        viewModelClass = SvgViewModel::class,
        categories = listOf(Category.GEMINI3)
    ),
    Sample(
        title = "Audio Summarization",
        description = "Use Gemini 3.5 Flash-Lite to summarize an audio file",
        route = AudioSummarizationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = AudioSummarizationViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING),
    ),
    Sample(
        title = "Summarize video",
        description = "Summarize a video and extract important dialogue.",
        route = VideoSummarizationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = VideoSummarizationViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING)
    ),
    Sample(
        title = "Translation from audio (Vertex AI)",
        description = "Translate an audio file stored in Cloud Storage",
        route = AudioTranslationRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = AudioTranslationViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING)
    ),
    Sample(
        title = "Blog post creator (Vertex AI)",
        description = "Create a blog post from an image file stored in Cloud Storage.",
        route = ImageBlogCreatorRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = ImageBlogCreatorViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING)
    ),
    Sample(
        title = "Gemini 3.1 Flash Image Lite (Nano Banana Lite)",
        description = "Generate and/or edit images using Nano Banana Lite",
        route = NanoBananaLiteRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = NanoBananaLiteViewModel::class,
        categories = listOf(Category.NANO_BANANA, Category.GEMINI3)
    ),
    Sample(
        title = "Gemini 3.1 Flash Image (Nano Banana 2)",
        description = "Generate and/or edit images using Nano Banana 2",
        route = NanoBanana2Route,
        screenType = ScreenType.CHAT,
        viewModelClass = NanoBanana2ViewModel::class,
        categories = listOf(Category.NANO_BANANA, Category.GEMINI3)
    ),
    Sample(
        title = "Gemini 3 Pro Image (Nano Banana Pro)",
        description = "Generate and/or edit images using Nano Banana Pro",
        route = NanoBananaProRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = NanoBananaProViewModel::class,
        categories = listOf(Category.NANO_BANANA, Category.GEMINI3)
    ),
    Sample(
        title = "Gemini 2.5 Flash Image (Nano Banana)",
        description = "Generate and/or edit images using Nano Banana",
        route = NanoBananaRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = NanoBananaViewModel::class,
        categories = listOf(Category.NANO_BANANA)
    ),
    Sample(
        title = "Document comparison (Vertex AI)",
        description = "Compare the contents of 2 documents stored in Cloud Storage",
        route = DocumentComparisonRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = DocumentComparisonViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING)
    ),
    Sample(
        title = "Hashtags for a video (Vertex AI)",
        description = "Generate hashtags for a video ad stored in Cloud Storage",
        route = VideoHashtagGeneratorRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = VideoHashtagGeneratorViewModel::class,
        categories = listOf(Category.MULTIMODAL_UNDERSTANDING)
    ),
    Sample(
        title = "Grounding with Google Search",
        description = "Use Grounding with Google Search to get responses based on up-to-date information from the" +
                " web.",
        route = GoogleSearchGroundingRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = GoogleSearchGroundingViewModel::class,
        categories = listOf(Category.TOOLS_FC)
    ),
    Sample(
        title = "Manual function calling",
        description = "Use function calling to get the weather conditions" +
                " for a specific US city on a specific date.",
        route = WeatherChatRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = WeatherChatViewModel::class,
        categories = listOf(Category.TOOLS_FC)
    ),
    Sample(
        title = "Automatic function calling",
        description = "Use automatic function calling to get the weather conditions" +
                " for a specific US city on a specific date.",
        route = AutoFunctionCallRoute,
        screenType = ScreenType.CHAT,
        viewModelClass = AutoFunctionCallViewModel::class,
        categories = listOf(Category.TOOLS_FC)
    ),
    Sample(
        title = "Gemini Live (audio input)",
        description = "Use bidirectional streaming to get information about" +
                " weather conditions for a specific US city on a specific date",
        route = StreamRealtimeAudioRoute,
        screenType = ScreenType.BIDI,
        viewModelClass = StreamAudioViewModel::class,
        categories = listOf(Category.LIVE_API, Category.TOOLS_FC)
    ),
    Sample(
        title = "Gemini Live (video input)",
        description = "Use bidirectional streaming to chat with Gemini using your" +
                " phone's camera",
        route = StreamRealtimeVideoRoute,
        screenType = ScreenType.BIDI_VIDEO,
        viewModelClass = StreamVideoViewModel::class,
        categories = listOf(Category.LIVE_API, Category.TOOLS_FC)
    ),
    Sample(
        title = "Server Prompt Templates - Gemini",
        description = "Generate an invoice using server prompt templates.  Note that you need to setup the template" +
                " in the Firebase console before running this demo.",
        route = ServerPromptTemplateRoute,
        screenType = ScreenType.SERVER_PROMPT,
        viewModelClass = ServerPromptTemplateViewModel::class,
        categories = listOf(Category.SERVER_PROMPTS),
    ),
    Sample(
        title = "Hybrid Receipt Scanner",
        description = "Use hybrid inference to scan receipts and extract expense data on-device whenever possible.",
        route = HybridInferenceRoute,
        screenType = ScreenType.HYBRID,
        viewModelClass = HybridInferenceViewModel::class,
        categories = listOf(Category.HYBRID)
    )
)
