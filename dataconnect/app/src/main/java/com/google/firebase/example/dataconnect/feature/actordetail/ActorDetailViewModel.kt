package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val actorDetailRoute = savedStateHandle.toRoute<ActorDetailRoute>()
    private val actorId: String = actorDetailRoute.actorId

    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val moviesConnector: MoviesConnector = MoviesConnector.instance

    private val _uiState = MutableStateFlow<ActorDetailUIState>(ActorDetailUIState.Loading)
    val uiState: StateFlow<ActorDetailUIState>
        get() = _uiState

    init {
        fetchActor()
    }

    private fun fetchActor() {
        viewModelScope.launch {
            try {
                val user = firebaseAuth.currentUser
                val actor = moviesConnector.getActorById.execute(
                    id = UUID.fromString(actorId)
                ).data.actor

                _uiState.value = ActorDetailUIState.Success(
                    actor = actor,
                    isUserSignedIn = user != null
                )
            } catch (e: Exception) {
                _uiState.value = ActorDetailUIState.Error(e.message)
            }
        }
    }
}