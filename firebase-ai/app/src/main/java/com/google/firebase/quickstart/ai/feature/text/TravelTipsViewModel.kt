package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.ChatUiState
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object TravelTipsRoute

class TravelTipsViewModel : ChatViewModel() {

    override val initialPrompt: String = "What else is important when traveling?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content {
                text(
                    "You are a Travel assistant. You will answer" +
                            " questions the user asks based on the information listed" +
                            " in Relevant Information. Do not hallucinate. Do not use" +
                            " the internet."
                )
            }
        )

        chat = generativeModel.startChat(
            history = listOf(
                content("role") {
                    text("I have never traveled before. When should I book a flight?")
                },
                content("model") {
                    text(
                        "You should book flights a couple of months ahead of time." +
                                " It will be cheaper and more flexible for you."
                    )
                },
                content("user") {
                    text("Do I need a passport?")
                },
                content("model") {
                    text(
                        "If you are traveling outside your own country, make sure" +
                                " your passport is up-to-date and valid for more" +
                                " than 6 months during your travel."
                    )
                }
            )
        )

        _messages.value = chat.history.map { UiChatMessage(it) }
        _uiState.value = ChatUiState.Success
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
