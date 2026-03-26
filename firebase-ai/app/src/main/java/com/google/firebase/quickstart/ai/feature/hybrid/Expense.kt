package com.google.firebase.quickstart.ai.feature.hybrid

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val name: String,
    val price: Double
)
