package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig

@Serializable
object ImageGenerationRoute

class ImageGenerationViewModel : ChatViewModel() {

    override val initialPrompt: String = "Hi, can you create a 3d rendered image of a pig " +
            "with wings and a top hat flying over a happy " +
            "futuristic scifi city with lots of greenery?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash-image",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
            }
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
