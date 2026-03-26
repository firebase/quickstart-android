package com.google.firebase.quickstart.ai.ui.navigation

import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

enum class Category(
    val label: String
) {
    TEXT("Text"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    FUNCTION_CALLING("Function calling"),
    LIVE_API("Live API Streaming")
}

enum class ScreenType {
    CHAT,
    IMAGEN,
    SVG,
    SERVER_PROMPT,
    BIDI,
    BIDI_VIDEO,
    HYBRID
}

data class Sample(
    val title: String,
    val description: String,
    val route: Any,
    val screenType: ScreenType,
    val viewModelClass: KClass<out ViewModel>? = null,
    val categories: List<Category>,
)
