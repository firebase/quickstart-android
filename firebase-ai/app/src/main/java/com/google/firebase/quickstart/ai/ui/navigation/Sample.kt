package com.google.firebase.quickstart.ai.ui.navigation

import android.graphics.Bitmap
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Tool
import com.google.firebase.quickstart.ai.feature.media.imagen.EditingMode
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
    // Optional parameters
    val modelName: String? = null,
    val backend: GenerativeBackend = GenerativeBackend.googleAI(),
    val initialPrompt: Content? = null,
    val systemInstructions: Content? = null,
    val generationConfig: GenerationConfig? = null,
    val chatHistory: List<Content> = emptyList(),
    val tools: List<Tool>? = null,
    val includeAttach: Boolean = false,
    val allowEmptyPrompt: Boolean = false,
    val additionalImage: Bitmap? = null,
    val imageLabels: List<String> = emptyList(),
    val selectionOptions: List<String> = emptyList(),
    val editingMode: EditingMode? = null,
)
