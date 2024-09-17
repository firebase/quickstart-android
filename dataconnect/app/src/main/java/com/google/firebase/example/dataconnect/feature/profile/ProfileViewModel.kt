package com.google.firebase.example.dataconnect.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.example.dataconnect.data.UserRepository
import com.google.firebase.example.dataconnect.feature.moviedetail.MovieDetailUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val auth: FirebaseAuth = Firebase.auth,
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUIState>(ProfileUIState.Loading)
    val uiState: StateFlow<ProfileUIState>
        get() = _uiState

    private val authStateListener: AuthStateListener

    init {
        authStateListener = object : AuthStateListener {
            override fun onAuthStateChanged(auth: FirebaseAuth) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    displayUser(currentUser.uid)
                } else {
                    _uiState.value = ProfileUIState.SignUpState
                }
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
                repository.addUser(displayName)
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message ?: "")
                e.printStackTrace()
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message ?: "")
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
                val user = repository.getUserById(userId)
                _uiState.value = ProfileUIState.ProfileState(
                    user?.username,
                    favoriteMovies = user?.favoriteMovies ?: emptyList()
                )
                Log.d("DisplayUser", "$user")
            } catch (e: Exception) {
                _uiState.value = ProfileUIState.Error(e.message ?: "")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}