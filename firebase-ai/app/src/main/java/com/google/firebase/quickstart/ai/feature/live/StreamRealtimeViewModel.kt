package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(PublicPreviewAPI::class)
class StreamRealtimeViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<StreamRealtimeRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }

    private lateinit var session: LiveSession

    fun connect() {
        val generativeModel = Firebase.ai(
            backend = sample.backend // GenerativeBackend.googleAI() by default
        ).liveModel(
            modelName = sample.modelName ?: "gemini-2.0-flash-live-preview-04-09",
            systemInstruction = sample.systemInstructions,
            generationConfig = liveGenerationConfig {
                responseModality = ResponseModality.AUDIO
                speechConfig = SpeechConfig(voice = Voice("ZEPHYR"))
            }
        )
        viewModelScope.launch {
            session = generativeModel.connect()
            session.startAudioConversation()
        }
    }

    fun start() {
        viewModelScope.launch {
            session.startAudioConversation()
        }
    }
}