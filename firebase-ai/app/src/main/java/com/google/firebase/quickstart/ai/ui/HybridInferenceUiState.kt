package com.google.firebase.quickstart.ai.ui

import com.google.firebase.quickstart.ai.feature.hybrid.Expense
import com.google.firebase.quickstart.ai.feature.hybrid.PersonSplit

data class HybridInferenceUiState(
    val expenses: List<Expense> = emptyList(),
    val isScanning: Boolean = false,
    val isCalculatingSplit: Boolean = false,
    val modelStatus: String = "Checking model status...",
    val errorMessage: String? = null,
    val splitResult: List<PersonSplit> = emptyList()
)
