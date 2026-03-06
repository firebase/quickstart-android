package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend

@Serializable
object VideoHashtagGeneratorRoute

class VideoHashtagGeneratorViewModel : ChatViewModel() {

    override val initialPrompt: String = "Generate 5-10 hashtags that relate to the video content." +
            " Try to use more popular and engaging terms," +
            " e.g. #Viral. Do not add content not related to" +
            " the video.\n Start the output with 'Tags:'"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = "gemini-2.5-flash"
        )
        chat = generativeModel.startChat()

        // Pre-attach the video
        contentBuilder.fileData(
            "https://storage.googleapis.com/cloud-samples-data/generative-ai/video/google_home_celebrity_ad.mp4",
            "video/mpeg"
        )
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
