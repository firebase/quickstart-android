package com.google.firebase.quickstart.ai.feature.text

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.TemplateGenerativeModel

@OptIn(PublicPreviewAPI::class)
class TextGenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<TextGenRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val allowEmptyPrompt = sample.allowEmptyPrompt

    val templateId = sample.templateId

    val templateKey = sample.templateKey

    private val _generatedText = MutableStateFlow<String?>(null)
    val generatedText: StateFlow<String?> = _generatedText

    // Firebase AI Logic
    private val generativeModel: GenerativeModel
    private val templateGenerativeModel: TemplateGenerativeModel

    init {
        generativeModel = Firebase.ai(
            backend = sample.backend // GenerativeBackend.googleAI() by default
        ).generativeModel(
            modelName = sample.modelName ?: "gemini-2.5-flash",
            systemInstruction = sample.systemInstructions,
            generationConfig = sample.generationConfig,
            tools = sample.tools
        )
        templateGenerativeModel = Firebase.ai.templateGenerativeModel()
    }

    fun generate(inputText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // clear error message
            try {
                val generativeResponse = if (templateId != null) {
                    templateGenerativeModel
                        .generateContent(templateId, mapOf(templateKey!! to inputText))
                } else {
                    generativeModel.generateContent(inputText)
                }
                _generatedText.value = generativeResponse.text
            } catch (e: Exception) {
                val errorMessage =
                    if ((e.localizedMessage?.contains("not found") == true) && (templateId != null)) {
                        "Template was not found, please verify that your project contains a template named \"$templateId\"."
                    } else {
                        e.localizedMessage
                    }
                _errorMessage.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
