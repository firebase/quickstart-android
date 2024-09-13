package com.google.firebase.example.dataconnect.feature.profile

sealed class ProfileUIState {
    data object Loading: ProfileUIState()

    data class Error(val errorMessage: String): ProfileUIState()

    data object SignUpState: ProfileUIState()

    data class ProfileState(
        val username: String?,
        // TODO: add reviews and favorites🔄
    ) : ProfileUIState()
}
