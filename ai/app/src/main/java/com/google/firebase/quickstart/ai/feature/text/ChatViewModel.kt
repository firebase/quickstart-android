package com.google.firebase.quickstart.ai.feature.text

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.asTextOrNull
import com.google.firebase.quickstart.ai.ui.navigation.FIREBASE_AI_SAMPLES
import com.google.firebase.vertexai.Chat
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        sample.chatHistory.map { content ->
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                isPending = false
            )
        }
    )
    val messages: StateFlow<List<ChatMessage>> =
        _messages


    private val generativeModel: GenerativeModel
    private val chat: Chat

    init {
        generativeModel = Firebase.vertexAI.generativeModel(
            "gemini-2.0-flash"
        )
        chat = generativeModel.startChat()
    }


}
