package com.google.firebase.quickstart.ai.ui

import com.google.firebase.quickstart.ai.feature.hybrid.Expense

data class HybridInferenceUiState(
    val expenses: List<Expense> = emptyList(),
    val isScanning: Boolean = false,
    val modelStatus: String = "Checking model status...",
    val errorMessage: String? = null
)
