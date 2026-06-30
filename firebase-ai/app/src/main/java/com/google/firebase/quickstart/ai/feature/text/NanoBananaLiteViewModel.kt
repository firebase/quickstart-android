package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.imageConfig
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object NanoBananaLiteRoute

class NanoBananaLiteViewModel : ChatViewModel() {

    override val initialPrompt: String = """
        A kawaii-style sticker of a happy red panda wearing a tiny bamboo hat.
        It's munching on a green bamboo leaf. The design features bold,
        clean outlines, simple cel-shading, and a vibrant color palette.
        The background must be white.
        """.trimIndent()

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.1-flash-lite-image", // aka Nano Banana Lite
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                imageConfig = imageConfig {
                    // Nano Banana Lite currently only supports 1K output
                    imageSize = ImageSize.SIZE_1K
                }
            }
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
