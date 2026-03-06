package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

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
import com.google.firebase.quickstart.ai.ui.SvgUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Serializable
class SvgRoute(val sampleId: String? = null)

class SvgViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SvgUiState>(SvgUiState.Success())
    val uiState: StateFlow<SvgUiState> = _uiState.asStateFlow()

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
        val currentSvgs = (_uiState.value as? SvgUiState.Success)?.svgs ?: emptyList()
        _uiState.value = SvgUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                val newSvg = response.text
                if (newSvg != null) {
                    _uiState.value = SvgUiState.Success(listOf(newSvg) + currentSvgs)
                } else {
                    _uiState.value = SvgUiState.Success(currentSvgs)
                }
            } catch (e: Exception) {
                _uiState.value = SvgUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}