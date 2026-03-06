package com.google.firebase.quickstart.ai.feature.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.TemplateGenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ServerPromptTemplateRoute

@OptIn(PublicPreviewAPI::class)
class ServerPromptTemplateViewModel : ViewModel() {
    val initialPrompt = "Jane Doe"
    val allowEmptyPrompt = false
    
    private val _uiState = MutableStateFlow<ServerPromptUiState>(ServerPromptUiState.Success())
    val uiState: StateFlow<ServerPromptUiState> = _uiState.asStateFlow()

    private val templateGenerativeModel: TemplateGenerativeModel = Firebase.ai.templateGenerativeModel()

    fun generate(inputText: String) {
        val currentState = (_uiState.value as? ServerPromptUiState.Success) ?: ServerPromptUiState.Success()
        viewModelScope.launch {
            _uiState.value = ServerPromptUiState.Loading
            try {
                val response = templateGenerativeModel
                    .generateContent("input-system-instructions", mapOf("customerName" to inputText))
                _uiState.value = ServerPromptUiState.Success(response.text)
            } catch (e: Exception) {
                _uiState.value = ServerPromptUiState.Error(
                    if (e.localizedMessage?.contains("not found") == true) {
                        "Template was not found, please verify that your project contains a template named \"input-system-instructions\"."
                    } else {
                        e.localizedMessage ?: "Unknown error"
                    }
                )
            }
        }
    }
}
