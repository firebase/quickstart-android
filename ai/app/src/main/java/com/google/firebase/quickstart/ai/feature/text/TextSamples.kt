package com.google.firebase.quickstart.ai.feature.text

import androidx.compose.runtime.Composable
import com.google.firebase.quickstart.ai.ui.shared.Category
import com.google.firebase.quickstart.ai.ui.shared.MenuScreen
import com.google.firebase.quickstart.ai.ui.shared.Sample

@Composable
fun TextSamples() {
    val textSamples = listOf(
        Sample("Blog post creator", "Create a blog post", "post", listOf(Category.TEXT)),
        Sample(
            "Describe video content",
            "Get a description of the contents of a video",
            "post",
            listOf(Category.VIDEO)
        ),
        Sample(
            "Audio diarization",
            "Segment an audio record by speaker labels",
            "post",
            listOf(Category.AUDIO)
        ),
        Sample(
            "Function calling",
            "Ask Gemini about the current weather",
            "post",
            listOf(Category.FUNCTION_CALL)
        ),
    )
    MenuScreen(
        filterTitle = "Filter by use case:",
        filters = Category.entries.toList(),
        samples = textSamples
    )
}