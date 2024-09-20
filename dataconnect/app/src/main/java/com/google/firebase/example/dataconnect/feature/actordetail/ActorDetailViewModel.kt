package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActorDetailViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) : ViewModel() {
    private var actorId: String = ""

    private val _uiState = MutableStateFlow<ActorDetailUIState>(ActorDetailUIState.Loading)
    val uiState: StateFlow<ActorDetailUIState>
        get() = _uiState

    fun setActorId(id: String) {
        actorId = id
        viewModelScope.launch {
            try {
                val user = firebaseAuth.currentUser
                val actor = moviesConnector.getActorById.execute(
                    id = UUID.fromString(actorId)
                ).data.actor

                _uiState.value = if (user == null) {
                    ActorDetailUIState.Success(actor, isUserSignedIn = false)
                } else {
                    val isFavorite = moviesConnector.getIfFavoritedActor.execute(
                        id = user.uid,
                        actorId = UUID.fromString(actorId)
                    ).data.favoriteActor != null

                    ActorDetailUIState.Success(
                        actor,
                        isUserSignedIn = true,
                        isFavorite = isFavorite
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ActorDetailUIState.Error(e.message ?: "")
            }
        }
    }

    fun toggleFavorite(newValue: Boolean) {
        viewModelScope.launch {
            try {
                if (newValue) {
                    moviesConnector.addFavoritedActor.execute(UUID.fromString(actorId))
                } else {
                    moviesConnector.deleteFavoriteActor.execute(
                        userId = firebaseAuth.currentUser?.uid ?: "",
                        actorId = UUID.fromString(actorId)
                    )
                }
                // Re-run the query to fetch the actor details
                setActorId(actorId)
            } catch (e: Exception) {
                _uiState.value = ActorDetailUIState.Error(e.message ?: "")
            }
        }
    }
}