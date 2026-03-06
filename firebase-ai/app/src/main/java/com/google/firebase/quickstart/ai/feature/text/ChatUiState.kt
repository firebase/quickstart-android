package com.google.firebase.quickstart.ai.feature.text

import android.graphics.Bitmap
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GroundingMetadata

/**
 * Meant to present attachments in the UI
 */
data class Attachment(
    val fileName: String,
    val image: Bitmap? = null // only for image attachments
)

/**
 * A wrapper for a model [Content] object that includes additional UI-specific metadata.
 */
data class UiChatMessage(
    val content: Content,
    val groundingMetadata: GroundingMetadata? = null,
)

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(
        val messages: List<UiChatMessage> = emptyList(),
        val attachments: List<Attachment> = emptyList(),
    ) : ChatUiState
    data class Error(val message: String) : ChatUiState
}
