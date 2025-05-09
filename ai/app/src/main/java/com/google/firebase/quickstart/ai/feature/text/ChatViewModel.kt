package com.google.firebase.quickstart.ai.feature.text

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
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messageList: MutableList<ChatMessage> =
        sample.chatHistory.map { ChatMessage(it) }.toMutableStateList()
    private val _messages = MutableStateFlow<List<ChatMessage>>(_messageList)
    val messages: StateFlow<List<ChatMessage>> =
        _messages

    private val generativeModel: GenerativeModel
    private val chat: Chat

    init {
        generativeModel = Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.0-flash",
            systemInstruction = sample.systemInstructions
        )
        chat = generativeModel.startChat(sample.chatHistory)
    }

    fun sendMessage(userMessage: String) {
        // Add a pending message
        _messageList.add(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
            )
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(userMessage)

                response.text?.let { modelResponse ->
                    _messageList.add(
                        ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                        )
                    )
                }
            } catch (e: Exception) {
                _messageList.add(
                    ChatMessage(
                        text = e.localizedMessage,
                        participant = Participant.ERROR
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

}
