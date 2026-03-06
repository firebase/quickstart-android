package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content

@Serializable
object VideoSummarizationRoute

class VideoSummarizationViewModel : ChatViewModel() {

    override val initialPrompt: String = "I have attached the video file. Provide a description of" +
            " the video. The description should also contain" +
            " anything important which people say in the video."

    private val chat: Chat

    init {
        val chatHistory = listOf(
            content { text("Can you help me with the description of a video file?") },
            content("model") {
                text(
                    "Sure! Click on the attach button below and choose a" +
                            " video file for me to describe."
                )
            }
        )

        _uiState.value = ChatUiState.Success(
            messages = chatHistory.map { UiChatMessage(it) },
            attachments = emptyList()
        )

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
