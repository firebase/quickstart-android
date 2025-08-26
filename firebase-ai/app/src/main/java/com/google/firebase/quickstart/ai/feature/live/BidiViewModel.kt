package com.google.firebase.quickstart.ai.feature.media.imagen

import android.Manifest
import android.graphics.Bitmap
import androidx.annotation.RequiresPermission
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.LiveGenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenAspectRatio
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.LiveServerContent
import com.google.firebase.ai.type.LiveServerMessage
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.app
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.feature.live.StreamRealtimeRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(PublicPreviewAPI::class)
class BidiViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<StreamRealtimeRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }

    // Firebase AI Logic
    private var liveSession: LiveSession

    init {
        val liveGenerationConfig = liveGenerationConfig {
            speechConfig = SpeechConfig(voice = Voice("CHARON"))
            responseModality = ResponseModality.AUDIO
            // Change this to ContentModality.TEXT if you want text output.
        }
        @OptIn(PublicPreviewAPI::class)
        val liveModel = FirebaseAI.getInstance(Firebase.app, GenerativeBackend.googleAI()).liveModel(
            "gemini-live-2.5-flash-preview",
            generationConfig = liveGenerationConfig,
            tools = listOf()
        )
        runBlocking {
            liveSession = liveModel.connect()
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startConversation() {
      liveSession.startAudioConversation()
    }

    fun endConversation() {
        liveSession.stopAudioConversation()
    }


}
