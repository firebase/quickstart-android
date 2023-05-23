package com.google.firebase.quickstart.auth.kotlin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FirebaseUIViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var status: String = "",
        var detail: String? = null,
        var isSignInVisible: Boolean = true
    )

    init {
        // Check if user is signed in (non-null) and update UI accordingly.
        showSignedInUser()
    }

    fun showSignedInUser() {
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)
    }

    fun signOut() {
        updateUiState(null)
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user != null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Firebase User: ${user.displayName}",
                    detail = "Firebase UID: ${user.uid}",
                    isSignInVisible = false
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Signed out",
                    detail = null,
                    isSignInVisible = true
                )
            }
        }
    }

    companion object {
        const val TAG = "FirebaseUIViewModel"
    }
}