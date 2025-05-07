package com.google.firebase.quickstart.ai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Bottom Bar
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    CHAT("Text prompts", Icons.Rounded.ChatBubbleOutline),
    MEDIA("Media studio", Icons.Default.PhotoLibrary),
    LIVE("Stream realtime", Icons.Default.Mic),
}

@Serializable
object MenuNav

// Within Chat
@Serializable
object ChatNavigation

@Serializable
data class TextSample(
    val initialPrompt: String = "",
)

// Within Media

// Live
