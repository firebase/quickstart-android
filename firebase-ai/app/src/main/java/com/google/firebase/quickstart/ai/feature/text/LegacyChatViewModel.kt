package com.google.firebase.quickstart.ai.feature.text

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES

class LegacyChatViewModel(
    savedStateHandle: SavedStateHandle
) : ChatViewModel() {

    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId!! }

    override val initialPrompt: String = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()

    private val chat: Chat

    init {
        _uiState.value = ChatUiState.Success(
            messages = sample.chatHistory.map { UiChatMessage(it) },
            attachments = emptyList()
        )

        val generativeModel = Firebase.ai(backend = sample.backend).generativeModel(
            modelName = sample.modelName ?: "gemini-2.5-flash",
            systemInstruction = sample.systemInstructions,
            generationConfig = sample.generationConfig,
            tools = sample.tools
        )
        chat = generativeModel.startChat(sample.chatHistory)
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        val candidate = response.candidates.first()
        _uiState.value = ChatUiState.Success(
            messages = currentMessages + UiChatMessage(candidate.content, candidate.groundingMetadata),
            attachments = emptyList()
        )
    }
}
