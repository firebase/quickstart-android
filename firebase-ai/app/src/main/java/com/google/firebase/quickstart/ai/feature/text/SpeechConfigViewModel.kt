package com.google.firebase.quickstart.ai.feature.text

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.MultiSpeakerVoiceConfig
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeakerVoiceConfig
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SpeechConfigRoute

@OptIn(PublicPreviewAPI::class)
class SpeechConfigViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isVertexAI = MutableStateFlow(false)
    val isVertexAI: StateFlow<Boolean> = _isVertexAI.asStateFlow()

    private val _latencyMs = MutableStateFlow<Long?>(null)
    val latencyMs: StateFlow<Long?> = _latencyMs.asStateFlow()

    private val _audioSize = MutableStateFlow<Int?>(null)
    val audioSize: StateFlow<Int?> = _audioSize.asStateFlow()

    private var activeAudioTrack: AudioTrack? = null

    fun setBackend(isVertex: Boolean) {
        _isVertexAI.value = isVertex
    }

    fun clearConversation() {
        _messages.value = emptyList()
        _audioSize.value = null
        stopAudio()
    }

    fun generateSpeech(
        prompt: String,
        modelName: String = "gemini-2.5-flash-preview-tts",
        languageCode: String = "cmn",
        isMultiSpeaker: Boolean = true,
        speaker1Name: String = "Speaker1",
        speaker1Voice: String = "Puck",
        speaker2Name: String = "Speaker2",
        speaker2Voice: String = "Charon"
    ) {
        _messages.value = _messages.value + "User: $prompt"
        _isLoading.value = true
        _latencyMs.value = null
        _audioSize.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val backend = if (_isVertexAI.value) {
                    GenerativeBackend.vertexAI()
                } else {
                    GenerativeBackend.googleAI()
                }

                val speechConfig = if (isMultiSpeaker) {
                    SpeechConfig(
                        multiSpeakerVoiceConfig = MultiSpeakerVoiceConfig(
                            speakerVoiceConfigs = listOf(
                                SpeakerVoiceConfig(speaker = speaker1Name, voice = Voice(speaker1Voice)),
                                SpeakerVoiceConfig(speaker = speaker2Name, voice = Voice(speaker2Voice))
                            )
                        ),
                        languageCode = languageCode
                    )
                } else {
                    SpeechConfig(
                        voice = Voice(speaker1Voice),
                        languageCode = languageCode
                    )
                }

                val generativeModel = Firebase.ai(backend = backend).generativeModel(
                    modelName = modelName,
                    generationConfig = generationConfig {
                        responseModalities = listOf(ResponseModality.AUDIO)
                        this.speechConfig = speechConfig
                    }
                )

                val response = generativeModel.generateContent(prompt)
                val endTime = System.currentTimeMillis()
                _latencyMs.value = endTime - startTime

                val candidate = response.candidates.firstOrNull()
                val parts = candidate?.content?.parts ?: emptyList()

                var textReply = ""
                var audioBytes: ByteArray? = null

                for (part in parts) {
                    when (part) {
                        is TextPart -> {
                            textReply += part.text
                        }
                        is InlineDataPart -> {
                            if (part.mimeType.startsWith("audio/")) {
                                audioBytes = part.inlineData
                            }
                        }
                    }
                }

                if (textReply.isEmpty()) {
                    textReply = "[Audio Response Only]"
                }

                val displayReply = "Model response: $textReply"
                _messages.value = _messages.value + displayReply

                if (audioBytes != null && audioBytes.isNotEmpty()) {
                    _audioSize.value = audioBytes.size
                    playAudio(audioBytes)
                } else {
                    _messages.value = _messages.value + "System: No audio data returned in response."
                }

            } catch (e: Exception) {
                Log.e("SpeechConfigViewModel", "Error generating speech", e)
                _messages.value = _messages.value + "Error: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun playAudio(pcmData: ByteArray) {
        stopAudio()
        try {
            val sampleRate = 24000
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize.coerceAtLeast(pcmData.size))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            activeAudioTrack = audioTrack
            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
        } catch (e: Exception) {
            Log.e("SpeechConfigViewModel", "Error playing audio", e)
        }
    }

    fun stopAudio() {
        activeAudioTrack?.let {
            try {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.e("SpeechConfigViewModel", "Error stopping audio", e)
            }
        }
        activeAudioTrack = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
