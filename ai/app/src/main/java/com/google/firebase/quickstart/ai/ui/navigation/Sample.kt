package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.vertexai.type.Content
import java.util.UUID

enum class Category(
    val label: String
) {
    TEXT("Text"),
    IMAGE("Image"),
//    VIDEO("Video"),
//    AUDIO("Audio"),
//    DOCUMENT("Document"),
    FUNCTION_CALL("Function calling"),
    STREAM_REALTIME("Stream realtime"),
}

data class Sample(
    val id: String = UUID.randomUUID().toString(), // used for navigation
    val title: String,
    val description: String,
    val navRoute: String,
    val categories: List<Category>,
    // Optional parameters
    val initialPrompt: Content? = null,
    val systemInstructions: String = "",
    val chatHistory: List<Content> = emptyList(),
)
