package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.ChatUiState
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object AudioSummarizationRoute

class AudioSummarizationViewModel : ChatViewModel() {

    override val initialPrompt: String =
        """
        I have attached the audio file. Please analyze it and summarize 
        the contents of the audio as bullet points.            
        """.trimIndent()

    private val chat: Chat

    init {
        val chatHistory = listOf(
            content { text("Can you help me summarize an audio file?") },
            content("model") {
                text(
                    "Of course! Click on the attach button" +
                            " below and choose an audio file for me to summarize."
                )
            }
        )

        _messages.value = chatHistory.map { UiChatMessage(it) }
        _uiState.value = ChatUiState.Success

        val generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash"
        )
        chat = generativeModel.startChat(chatHistory)
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
