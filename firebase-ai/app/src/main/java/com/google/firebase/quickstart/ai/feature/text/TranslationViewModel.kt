package com.google.firebase.quickstart.ai.feature.text

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.UiChatMessage
import kotlinx.serialization.Serializable

@Serializable
object TranslationRoute

class TranslationViewModel : ChatViewModel() {
    override val initialPrompt: String
        get() = """
            Translate the following text to Spanish:
            Hey, are you down to grab some pizza later? I'm starving!
        """.trimIndent()

    private var chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            systemInstruction = content {
                text("Only output the translated text")
            }
        )

        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(
        prompt: Content,
        currentMessages: List<UiChatMessage>
    ) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}