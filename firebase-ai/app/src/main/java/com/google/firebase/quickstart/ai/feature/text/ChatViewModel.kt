package com.google.firebase.quickstart.ai.feature.text

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.quickstart.ai.ui.Attachment
import com.google.firebase.quickstart.ai.ui.ChatUiState
import com.google.firebase.quickstart.ai.ui.UiChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
abstract class ChatViewModel : ViewModel() {

    protected val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Success)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    protected val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    protected val _attachments = MutableStateFlow<List<Attachment>>(emptyList())
    val attachments: StateFlow<List<Attachment>> = _attachments.asStateFlow()

    abstract val initialPrompt: String

    // Builder for the next message
    protected var contentBuilder = Content.Builder()

    /**
     * Entry point for sending a message.
     * Handles adding the message to the UI and setting the loading state.
     */
    fun sendMessage(userMessage: String) {
        val prompt = contentBuilder
            .text(userMessage)
            .build()

        _messages.value = _messages.value + UiChatMessage(prompt)

        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                performSendMessage(prompt, _messages.value)
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "Unknown error")
            } finally {
                contentBuilder = Content.Builder() // reset the builder
            }
        }
    }

    /**
     * Subclasses implement this to handle the actual AI logic.
     */
    protected abstract suspend fun performSendMessage(
        prompt: Content,
        currentMessages: List<UiChatMessage>
    )

    /**
     * Centralized method to validate the AI response (grounding check) and update the UI state.
     */
    protected fun validateAndDisplayResponse(
        response: GenerateContentResponse,
        currentMessages: List<UiChatMessage>
    ) {
        val candidate = response.candidates.firstOrNull() ?: return

        // Compliance check for grounding
        if (candidate.groundingMetadata != null
            && candidate.groundingMetadata?.groundingChunks?.isNotEmpty() == true
            && candidate.groundingMetadata?.searchEntryPoint == null
        ) {
            _uiState.value = ChatUiState.Error(
                "Could not display the response because it was missing required attribution components."
            )
        } else {
            _messages.value = currentMessages + UiChatMessage(candidate.content, candidate.groundingMetadata)
            _attachments.value = emptyList()
            _uiState.value = ChatUiState.Success
        }
    }

    fun attachFile(
        fileInBytes: ByteArray,
        mimeType: String?,
        fileName: String? = "Unnamed file"
    ) {
        if (mimeType?.contains("image") == true) {
            // images should be attached as ImageParts
            contentBuilder.image(decodeBitmapFromImage(fileInBytes))
        } else {
            contentBuilder.inlineData(fileInBytes, mimeType ?: "text/plain")
        }

        _attachments.value = _attachments.value + Attachment(fileName ?: "Unnamed file")
    }

    protected fun decodeBitmapFromImage(input: ByteArray) =
        BitmapFactory.decodeByteArray(input, 0, input.size)
}
