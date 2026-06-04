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
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
abstract class ChatViewModel : ViewModel() {

    val uiState: StateFlow<ChatUiState>
        field = MutableStateFlow<ChatUiState>(ChatUiState.Success)

    val messages: StateFlow<List<UiChatMessage>>
        field = MutableStateFlow<List<UiChatMessage>>(emptyList())

    val attachments: StateFlow<List<Attachment>>
        field = MutableStateFlow<List<Attachment>>(emptyList())

    protected fun updateUiState(state: ChatUiState) {
        uiState.value = state
    }

    protected fun updateMessages(list: List<UiChatMessage>) {
        messages.value = list
    }

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

        messages.value = messages.value + UiChatMessage(prompt)

        viewModelScope.launch {
            uiState.value = ChatUiState.Loading
            try {
                performSendMessage(prompt, messages.value)
            } catch (e: Exception) {
                uiState.value = ChatUiState.Error(e.localizedMessage ?: "Unknown error")
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
            uiState.value = ChatUiState.Error(
                "Could not display the response because it was missing required attribution components."
            )
        } else {
            messages.value = currentMessages + UiChatMessage(candidate.content, candidate.groundingMetadata)
            attachments.value = emptyList()
            uiState.value = ChatUiState.Success
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

        attachments.value = attachments.value + Attachment(fileName ?: "Unnamed file")
    }

    protected fun decodeBitmapFromImage(input: ByteArray) =
        BitmapFactory.decodeByteArray(input, 0, input.size)
}
