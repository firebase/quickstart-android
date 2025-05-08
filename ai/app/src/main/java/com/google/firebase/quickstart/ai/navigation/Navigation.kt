package com.google.firebase.quickstart.ai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable



// Within Chat
@Serializable
data class TextSample(
    val initialPrompt: String = "",
)

// Within Media

// Live
