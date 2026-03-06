package com.google.firebase.quickstart.ai.feature.text

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content

class TravelTipsViewModel : ChatViewModel() {

    override val initialPrompt: String = "What else is important when traveling?"

    private val chatHistory = listOf(
        content {
            role = "user"
            text("I have never traveled before. When should I book a flight?")
        },
        content {
            role = "model"
            text(
                "You should book flights a couple of months ahead of time." +
                        " It will be cheaper and more flexible for you."
            )
        },
        content {
            role = "user"
            text("Do I need a passport?")
        },
        content {
            role = "model"
            text(
                "If you are traveling outside your own country, make sure" +
                        " your passport is up-to-date and valid for more" +
                        " than 6 months during your travel."
            )
        }
    )

    private val chat: Chat

    init {
        _uiState.value = ChatUiState.Success(
            messages = chatHistory.map { UiChatMessage(it) },
            attachments = emptyList()
        )

        val generativeModel = Firebase.ai.generativeModel(
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
        chat = generativeModel.startChat(chatHistory)
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        val candidate = response.candidates.first()

        // Compliance check for grounding (common logic could be in ChatViewModel if reused)
        if (candidate.groundingMetadata != null
            && candidate.groundingMetadata?.groundingChunks?.isNotEmpty() == true
            && candidate.groundingMetadata?.searchEntryPoint == null
        ) {
            _uiState.value = ChatUiState.Error(
                "Could not display the response because it was missing required attribution components."
            )
        } else {
            _uiState.value = ChatUiState.Success(
                messages = currentMessages + UiChatMessage(candidate.content, candidate.groundingMetadata),
                attachments = emptyList()
            )
        }
    }
}
