package com.google.firebase.example.dataconnect.feature.actordetail

import com.google.firebase.dataconnect.movies.GetActorByIdQuery
import com.google.firebase.dataconnect.movies.GetMovieByIdQuery


sealed class ActorDetailUIState {
    data object Loading: ActorDetailUIState()

    data class Error(val errorMessage: String?): ActorDetailUIState()

    data class Success(
        // Actor is null if it can't be found on the DB
        val actor: GetActorByIdQuery.Data.Actor?,
        val isUserSignedIn: Boolean = false,
    ) : ActorDetailUIState()
}
