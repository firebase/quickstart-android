package com.google.firebase.example.dataconnect.feature.profile

import com.google.firebase.dataconnect.movies.GetUserByIdQuery

sealed class ProfileUIState {
    data object Loading: ProfileUIState()

    data class Error(val errorMessage: String?): ProfileUIState()

    data object AuthState: ProfileUIState()

    data class ProfileState(
        val username: String?,
        val reviews: List<GetUserByIdQuery.Data.User.ReviewsItem>? = emptyList(),
        val watchedMovies: List<GetUserByIdQuery.Data.User.WatchedItem>? = emptyList(),
        val favoriteMovies: List<GetUserByIdQuery.Data.User.FavoriteMoviesItem>? = emptyList(),
        val favoriteActors: List<GetUserByIdQuery.Data.User.FavoriteActorsItem>? = emptyList()
    ) : ProfileUIState()
}
