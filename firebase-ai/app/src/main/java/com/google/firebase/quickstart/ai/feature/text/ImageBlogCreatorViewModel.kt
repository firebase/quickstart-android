package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object ImageBlogCreatorRoute

class ImageBlogCreatorViewModel : ChatViewModel() {

    override val initialPrompt: String = "Write a short, engaging blog post based on this picture." +
            " It should include a description of the meal in the" +
            " photo and talk about my journey meal prepping."

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = "gemini-2.5-flash"
        )
        chat = generativeModel.startChat()

        // Pre-attach the image from cloud storage
        contentBuilder.fileData(
            "https://storage.googleapis.com/cloud-samples-data/generative-ai/image/meal-prep.jpeg",
            "image/jpeg"
        )
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
