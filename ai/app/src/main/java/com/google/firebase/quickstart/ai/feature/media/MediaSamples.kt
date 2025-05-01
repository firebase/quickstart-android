package com.google.firebase.quickstart.ai.feature.media

import androidx.compose.runtime.Composable
import com.google.firebase.quickstart.ai.ui.shared.Category
import com.google.firebase.quickstart.ai.ui.shared.MenuScreen
import com.google.firebase.quickstart.ai.ui.shared.Sample

@Composable
fun MediaSamples() {
    val mediaSamples = listOf(
        Sample(
            "Imagen",
            "Generate images with the Imagen 3 model",
            "imagen",
            listOf(Category.IMAGE)
        ),
        Sample(
            "Veo",
            "Generate 8 seconds videos with Veo 2",
            "veo",
            listOf(Category.VIDEO)
        ),
    )
    MenuScreen(
        filterTitle = "Filter by modality:",
        filters = listOf(Category.IMAGE, Category.VIDEO),
        samples = mediaSamples
    )
}