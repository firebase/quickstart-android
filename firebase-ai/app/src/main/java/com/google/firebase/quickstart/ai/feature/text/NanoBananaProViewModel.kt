package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.imageConfig
import com.google.firebase.quickstart.ai.ui.UiChatMessage

@Serializable
object NanoBananaProRoute

class NanoBananaProViewModel : ChatViewModel() {

    override val initialPrompt: String = """
        Present a clear, 45° top-down isometric miniature 3D cartoon
        scene of London, featuring its most iconic landmarks and
        architectural elements. Use soft, refined textures with
        realistic PBR materials and gentle, lifelike lighting and shadows.
        Integrate the current weather conditions directly into the city
        environment to create an immersive atmospheric mood. Use a clean,
        minimalistic composition with a soft, solid-colored background.
        At the top-center, place the title "London" in large bold text,
        a prominent weather icon beneath it, then the date (small text)
        and temperature (medium text). All text must be centered with
        consistent spacing, and may subtly overlap the tops of the buildings.
        """.trimIndent()

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3-pro-image",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                // Optionally specify additional configuration
                imageConfig = imageConfig {
                    aspectRatio = AspectRatio.SQUARE_1x1
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
