package com.google.firebase.quickstart.ai.ui.navigation

import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

enum class Category(
    val label: String
) {
    GEMINI3("Gemini 3"),
    NANO_BANANA("Nano Banana"),
    MULTIMODAL_UNDERSTANDING("Multimodal understanding"),
    TOOLS_FC("Tools and Function calling"),
    LIVE_API("Live API streaming"),
    HYBRID_AI("Hybrid inference"),
    SERVER_PROMPTS("Server prompt templates"),
    IMAGEN("Imagen")
}

enum class ScreenType {
    CHAT,
    IMAGEN,
    SVG,
    SERVER_PROMPT,
    BIDI,
    BIDI_VIDEO
}

data class Sample(
    val title: String,
    val description: String,
    val route: Any,
    val screenType: ScreenType,
    val viewModelClass: KClass<out ViewModel>? = null,
    val categories: List<Category>,
)
