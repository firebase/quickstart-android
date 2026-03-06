package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content

@Serializable
object CourseRecommendationsRoute

class CourseRecommendationsViewModel : ChatViewModel() {

    override val initialPrompt: String = "I am interested in Performing Arts. I have taken Theater 1A."

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content {
                text(
                    "You are a chatbot for the county's performing and fine arts" +
                            " program. You help students decide what course they will" +
                            " take during the summer."
                )
            }
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
