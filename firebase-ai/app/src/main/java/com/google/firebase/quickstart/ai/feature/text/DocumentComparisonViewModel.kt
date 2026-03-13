package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object DocumentComparisonRoute

class DocumentComparisonViewModel : ChatViewModel() {

    override val initialPrompt: String = "The first document is from 2013, and the second document is" +
            " from 2023. How did the standard deduction evolve?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = "gemini-2.5-flash"
        )
        chat = generativeModel.startChat()

        // Pre-attach the documents
        contentBuilder.fileData(
            "https://storage.googleapis.com/cloud-samples-data/generative-ai/pdf/form_1040_2013.pdf",
            "application/pdf"
        )
        contentBuilder.fileData(
            "https://storage.googleapis.com/cloud-samples-data/generative-ai/pdf/form_1040_2023.pdf",
            "application/pdf"
        )
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
