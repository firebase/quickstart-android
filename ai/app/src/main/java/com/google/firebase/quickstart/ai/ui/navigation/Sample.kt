package com.google.firebase.quickstart.ai.ui.navigation

enum class Category(
    val label: String
) {
    TEXT("Text"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    FUNCTION_CALL("Function calling"),
    STREAM_REALTIME("Stream realtime"),
}

data class Sample(
    val title: String,
    val description: String,
    val destination: String,
    val categories: List<Category>
)
