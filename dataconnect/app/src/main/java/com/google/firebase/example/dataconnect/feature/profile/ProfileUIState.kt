package com.google.firebase.example.dataconnect.feature.profile

import com.google.firebase.dataconnect.movies.GetCurrentUserQuery

sealed class ProfileUIState {
    data object Loading: ProfileUIState()

    data class Error(val errorMessage: String?): ProfileUIState()

    data object AuthState: ProfileUIState()

    data class ProfileState(
        val username: String?,
        val reviews: List<GetCurrentUserQuery.Data.User.ReviewsItem>? = emptyList(),
        val favoriteMovies: List<GetCurrentUserQuery.Data.User.FavoriteMoviesItem>? = emptyList(),
    ) : ProfileUIState()
}
