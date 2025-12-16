package com.google.firebase.quickstart.ai.feature.svg

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.feature.text.ChatRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SvgViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt: String =
        sample.initialPrompt?.parts
            ?.filterIsInstance<TextPart>()
            ?.first()
            ?.asTextOrNull().orEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _generatedSvgList = mutableStateListOf<String>()
    val generatedSvgs: StateFlow<List<String>> =
        MutableStateFlow<List<String>>(_generatedSvgList)

    private val generativeModel: GenerativeModel

    init {
        generativeModel = Firebase.ai(
            backend = sample.backend
        ).generativeModel(
            modelName = sample.modelName ?: "gemini-2.5-flash",
            systemInstruction = sample.systemInstructions,
            generationConfig = sample.generationConfig,
            tools = sample.tools
        )
    }

    fun generateSVG(prompt: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let {
                    _generatedSvgList.addFirst(it)
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }

    }
}