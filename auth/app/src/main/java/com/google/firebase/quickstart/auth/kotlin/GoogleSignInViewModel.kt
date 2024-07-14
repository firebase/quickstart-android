package com.google.firebase.quickstart.auth.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleSignInViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var status: String = "",
        var detail: String? = null,
        var isSignInVisible: Boolean = true,
        var isProgressBarVisible: Boolean = false,
        var isOneTapUiShown: Boolean = false
    )

    init {
        // Check if user is signed in (non-null) and update UI accordingly.
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)
        if (firebaseUser == null) {
            _uiState.update { it.copy(isOneTapUiShown = true) }
        }
    }

    fun showInitialState() {
        updateUiState(null)
    }

    fun signInWithFirebase(googleIdToken: String) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        viewModelScope.launch {
            toggleProgressbar(isVisible = true)
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", e)
                // TODO(thatfiredev) "Authentication Failed."
                updateUiState(null)
            } finally {
                toggleProgressbar(isVisible = false)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user != null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Google User: ${user.displayName}",
                    detail = "Firebase UID: ${user.uid}",
                    isSignInVisible = false,
                    isProgressBarVisible = false
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Signed out",
                    detail = null,
                    isSignInVisible = true,
                    isProgressBarVisible = false
                )
            }
        }
    }

    private fun toggleProgressbar(isVisible: Boolean) {
        _uiState.update { it.copy(isProgressBarVisible = isVisible) }
    }

    companion object {
        const val TAG = "GoogleSignInViewModel"
    }
}