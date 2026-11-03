package com.google.firebase.quickstart.ai.ui

data class FunctionCallInfo(
    val name: String,
    val args: String,
    val id: String? = null
)

data class FunctionCallingUiState(
    val modelStatus: String = "Checking model status...",
    val isLoading: Boolean = false,
    val functionCalls: List<FunctionCallInfo> = emptyList(),
    val functionExecutionResult: String? = null,
    val responseText: String? = null,
    val inferenceSource: String? = null,
    val logOutput: String = "Configure function calling options above and tap Run.",
    val errorMessage: String? = null
)
