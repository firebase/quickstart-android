package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.ai.type.PublicPreviewAPI
import java.util.UUID

enum class Category(
    val label: String
) {
    TEXT("Text"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    FUNCTION_CALLING("Function calling"),
    LIVE_API("LiveAPI Streaming")
}

@OptIn(PublicPreviewAPI::class)
data class Sample(
    val id: String = UUID.randomUUID().toString(), // used for navigation
    val title: String,
    val description: String,
    val navRoute: String,
    val categories: List<Category>,
)
