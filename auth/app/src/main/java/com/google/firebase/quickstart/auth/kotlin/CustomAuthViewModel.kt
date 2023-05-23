package com.google.firebase.quickstart.auth.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CustomAuthViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var customToken: String? = null

    data class UiState(
        var signInStatus: String = "",
        var tokenStatus: String = "Token:null",
        var isSignInEnabled: Boolean = false,
    )

    init {
        // Check if user is signed in (non-null) and update UI accordingly.
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)
    }

    fun setCustomToken(customToken: String?) {
        this.customToken = customToken

        // Enable/disable sign-in button and show the token
        _uiState.update { it.copy(isSignInEnabled = true, tokenStatus = "Token:$customToken") }
    }

    fun startSignIn() {
        customToken?.let { token ->
            // Initiate sign in with custom token
            viewModelScope.launch {
                try {
                    val authResult = firebaseAuth.signInWithCustomToken(token).await()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCustomToken:success")
                    updateUiState(authResult.user)
                } catch (e: Exception) {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCustomToken:failure", e)
                    // TODO(thatfiredev): Show "Authentication failed" snackbar
                    updateUiState(null)
                }
            }
        }
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user != null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    signInStatus = "User ID: ${user.uid}"
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    signInStatus = "Error: sign in failed"
                )
            }
        }
    }

    companion object {
        private const val TAG = "CustomAuthViewModel"
    }
}