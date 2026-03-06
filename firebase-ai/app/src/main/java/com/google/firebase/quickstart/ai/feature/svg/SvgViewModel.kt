package com.google.firebase.quickstart.ai.feature.svg

import kotlinx.serialization.Serializable

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Serializable
class SvgRoute(val sampleId: String? = null)

class SvgViewModel : ViewModel() {
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
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3-flash-preview",
            systemInstruction = content {
                text(
                    """
            You are an expert at turning image prompts into SVG code. When given a prompt,
            use your creativity to code a 800x600 SVG rendering of it.
            Always add viewBox="0 0 800 600" to the root svg tag. Do
            not import external assets, they won't work. Return ONLY the SVG code, nothing else,
            no commentary.
            """.trimIndent()
                )
            },
            generationConfig = generationConfig {
                thinkingConfig {
                    thinkingBudget = -1
                }
            }
        )
    }

    fun generateSVG(prompt: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let {
                    _generatedSvgList.add(0, it)
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