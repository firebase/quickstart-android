package com.google.firebase.quickstart.ai.feature.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.TemplateGenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.quickstart.ai.ui.ServerPromptUiState
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
    
    val uiState: StateFlow<ServerPromptUiState>
        field = MutableStateFlow<ServerPromptUiState>(ServerPromptUiState.Success())

    private var templateGenerativeModel: TemplateGenerativeModel

    init {
        templateGenerativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).templateGenerativeModel()
    }

    fun generate(inputText: String) {
        viewModelScope.launch {
            uiState.value = ServerPromptUiState.Loading
            try {
                val response = templateGenerativeModel
                    .generateContent("input-system-instructions", mapOf("customerName" to inputText))
                uiState.value = ServerPromptUiState.Success(response.text)
            } catch (e: Exception) {
                uiState.value = ServerPromptUiState.Error(
                    if (e.localizedMessage?.contains("not found") == true) {
                        """
                        Template was not found, please verify that your project contains a template 
                        named "input-system-instructions".   
                        """.trimIndent()
                    } else {
                        e.localizedMessage ?: "Unknown error"
                    }
                )
            }
        }
    }
}
