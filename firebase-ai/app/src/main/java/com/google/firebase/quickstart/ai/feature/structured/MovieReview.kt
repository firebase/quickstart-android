package com.google.firebase.quickstart.ai.feature.structured

import com.google.firebase.ai.annotations.Generable
import com.google.firebase.ai.annotations.Guide
import kotlinx.serialization.Serializable

@Generable(description = "A structured review and analysis of a movie or TV show")
@Serializable
data class MovieReview(
    @Guide(description = "The official title of the movie or TV show")
    val title: String,
    @Guide(description = "A concise 1-sentence summary of the critique")
    val summary: String,
    @Guide(description = "The rating out of 5 stars (1 to 5)")
    val rating: Int,
    @Guide(description = "Key genre tags or themes")
    val tags: List<String>
) {
    companion object
}
