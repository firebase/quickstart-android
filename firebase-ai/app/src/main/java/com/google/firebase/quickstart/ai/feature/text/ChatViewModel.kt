package com.google.firebase.quickstart.ai.feature.text

import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FileDataPart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GroundingMetadata
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A wrapper for a model [Content] object that includes additional UI-specific metadata.
 */
data class UiChatMessage(
    val content: Content,
    val groundingMetadata: GroundingMetadata? = null,
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt: String =
        sample.initialPrompt?.parts
            ?.filterIsInstance<TextPart>()
            ?.first()
            ?.asTextOrNull().orEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _messageList: MutableList<UiChatMessage> =
        sample.chatHistory.map { UiChatMessage(it) }.toMutableStateList()
    private val _messages = MutableStateFlow<List<UiChatMessage>>(_messageList)
    val messages: StateFlow<List<UiChatMessage>> =
        _messages

    private val _attachmentsList: MutableList<Attachment> =
        sample.initialPrompt?.parts?.filterIsInstance<FileDataPart>()?.map {
            Attachment(it.uri)
        }?.toMutableStateList() ?: mutableStateListOf()
    private val _attachments = MutableStateFlow<List<Attachment>>(_attachmentsList)
    val attachments: StateFlow<List<Attachment>>
        get() = _attachments

    // Firebase AI Logic
    private var contentBuilder = Content.Builder()
    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = sample.modelName ?: "gemini-2.0-flash",
            systemInstruction = sample.systemInstructions,
            generationConfig = sample.generationConfig,
            tools = sample.tools
        )
        chat = generativeModel.startChat(sample.chatHistory)

        // add attachments from initial prompt
        sample.initialPrompt?.parts?.forEach { part ->
            if (part is TextPart) {
                /* Ignore text parts, as the text will come from the textInputField */
            } else {
                contentBuilder.part(part)
            }
        }
    }

    fun sendMessage(userMessage: String) {
        val prompt = contentBuilder
            .text(userMessage)
            .build()

        _messageList.add(UiChatMessage(prompt))

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(prompt)
                val candidate = response.candidates.first()

                // Compliance check for grounding
                if (candidate.groundingMetadata != null
                    && candidate.groundingMetadata?.groundingChunks?.isNotEmpty() == true
                    && candidate.groundingMetadata?.searchEntryPoint == null) {
                    _errorMessage.value =
                        "Could not display the response because it was missing required attribution components."
                } else {
                    _messageList.add(
                        UiChatMessage(candidate.content, candidate.groundingMetadata)
                    )
                    _errorMessage.value = null // clear errors
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
                contentBuilder = Content.Builder() // reset the builder
                _attachmentsList.clear()
            }
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
        _attachmentsList.add(Attachment(fileName ?: "Unnamed file"))
    }

    private fun decodeBitmapFromImage(input: ByteArray) =
        BitmapFactory.decodeByteArray(input, 0, input.size)
}