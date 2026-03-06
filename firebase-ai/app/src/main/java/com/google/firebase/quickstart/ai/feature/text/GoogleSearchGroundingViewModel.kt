package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Tool
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object GoogleSearchGroundingRoute

class GoogleSearchGroundingViewModel : ChatViewModel() {

    override val initialPrompt: String = "What's the weather in Chicago this weekend?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            tools = listOf(Tool.googleSearch())
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
