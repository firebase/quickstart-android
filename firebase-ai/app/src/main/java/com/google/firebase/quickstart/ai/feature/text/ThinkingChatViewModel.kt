package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig

@Serializable
object ThinkingChatRoute

class ThinkingChatViewModel : ChatViewModel() {

    override val initialPrompt: String = "Analogize photosynthesis and growing up."

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig {
                    includeThoughts = true
                    thinkingBudget = -1 // Dynamic Thinking
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
