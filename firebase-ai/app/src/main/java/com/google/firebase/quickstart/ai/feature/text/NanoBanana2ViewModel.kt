package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object NanoBanana2Route

class NanoBanana2ViewModel : ChatViewModel() {

    override val initialPrompt: String = """
        A photo of a glossy magazine cover, the minimal blue cover
        has the large bold words Nano Banana. The text is in a serif
        font and fills the view. No other text. In front of the text
        there is a portrait of a person in a sleek and minimal dress.
        She is playfully holding the number 2, which is the focal point.
        Put the issue number and "Feb 2026" date in the corner along with
        a barcode. The magazine is on a shelf against an orange plastered
        wall, within a designer store.
        """.trimIndent()

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.1-flash-image-preview",
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
