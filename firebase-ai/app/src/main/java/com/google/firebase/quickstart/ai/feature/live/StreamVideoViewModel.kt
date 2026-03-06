package com.google.firebase.quickstart.ai.feature.live

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
object StreamRealtimeVideoRoute

@OptIn(PublicPreviewAPI::class)
class StreamVideoViewModel : BidiViewModel() {
    init {
        val liveGenerationConfig = liveGenerationConfig {
            speechConfig = SpeechConfig(voice = Voice("CHARON"))
            responseModality = ResponseModality.AUDIO
        }

        val liveModel =
            Firebase.ai(backend = GenerativeBackend.googleAI())
                .liveModel(
                    modelName = "gemini-2.5-flash-native-audio-preview-09-2025",
                    generationConfig = liveGenerationConfig,
                )
        runBlocking { liveSession = liveModel.connect() }
    }
}
