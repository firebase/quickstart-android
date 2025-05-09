package com.google.firebase.quickstart.ai.feature.text

import android.graphics.Bitmap
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.asTextOrNull
import com.google.firebase.quickstart.ai.ui.navigation.FIREBASE_AI_SAMPLES
import com.google.firebase.vertexai.Chat
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.Content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _messageList: MutableList<Content> =
        sample.chatHistory.toMutableStateList()
    private val _messages = MutableStateFlow<List<Content>>(_messageList)
    val messages: StateFlow<List<Content>> =
        _messages

    private val generativeModel: GenerativeModel
    private val chat: Chat
    private var contentBuilder = Content.Builder()

    init {
        generativeModel = Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.0-flash",
            systemInstruction = sample.systemInstructions
        )
        chat = generativeModel.startChat(sample.chatHistory)
    }

    fun sendMessage(userMessage: String) {
        val prompt = contentBuilder
            .text(userMessage)
            .build()

        _messageList.add(prompt)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(prompt)
                _messageList.add(response.candidates.first().content)
                _errorMessage.value = null // clear errors
                contentBuilder = Content.Builder() // reset the builder
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun attachFile(fileInBytes: ByteArray, mimeType: String?) {
        contentBuilder.inlineData(fileInBytes, mimeType ?: "text/plain")
    }
}
