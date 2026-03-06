package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object AudioTranslationRoute

class AudioTranslationViewModel : ChatViewModel() {

    override val initialPrompt: String = "Please translate the audio to Mandarin."

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = "gemini-2.5-flash"
        )
        chat = generativeModel.startChat()
        
        // Handling the initial fileData in the prompt builder for the first message
        contentBuilder.fileData(
            "https://storage.googleapis.com/cloud-samples-data/generative-ai/audio/" +
                    "How_to_create_a_My_Map_in_Google_Maps.mp3",
            "audio/mpeg"
        )
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }
}
