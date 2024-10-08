package com.google.firebase.example.dataconnect.feature.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUIState>(ProfileUIState.Loading)
    val uiState: StateFlow<ProfileUIState>
        get() = _uiState

    private val authStateListener: AuthStateListener

    init {
        authStateListener = AuthStateListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                displayUser(currentUser.uid)
            } else {
                _uiState.value = ProfileUIState.AuthState
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    fun signUp(
        email: String,
        password: String,
        displayName: String
    ) {
        viewModelScope.launch {
            try {
                val signInResult = auth.createUserWithEmailAndPassword(email, password).await()
                signInResult.user?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                )?.await()
                moviesConnector.upsertUser.execute(username = displayName)
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message)
                e.printStackTrace()
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun displayUser(
        userId: String
    ) {
        viewModelScope.launch {
            try {
                val user = moviesConnector.getCurrentUser.execute().data.user
                _uiState.value = ProfileUIState.ProfileState(
                    user?.username,
                    favoriteMovies = user?.favoriteMovies,
                    reviews = user?.reviews
                )
                Log.d("DisplayUser", "$user")
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}