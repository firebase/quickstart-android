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
    val tags: List<String>,
    @Guide(description = "The main character performance in the movie")
    val mainPerformance: CharacterPerformance,
    @Guide(description = "Details about the cast and crew")
    val castInfo: CastInfo
) {
    @Generable
    @Serializable
    data class CharacterPerformance(
        @Guide(description = "The name of the actor")
        val actorName: String,
        val performanceRating: Int
    )

    companion object
}

@Generable
@Serializable
data class CastInfo(
    val castingDirector: String,
    @Guide(description = "The lead actor's performance")
    val leadActor: MovieReview.CharacterPerformance,
    @Guide(description = "A list of supporting actors' performances")
    val supportingActors: List<MovieReview.CharacterPerformance>
)
