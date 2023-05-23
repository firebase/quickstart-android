package com.google.firebase.quickstart.auth.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnonymousAuthViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var userId: String? = null,
        var userEmail: String? = null,
        var isSignInEnabled: Boolean = true,
        var isSignOutEnabled: Boolean = false,
        var isLinkAccountEnabled: Boolean = false,
        var isProgressBarVisible: Boolean = false,
        var emailError: String? = null,
        var passwordError: String?= null,
    )

    init {
        // Check if there's a user signed in and update UI State accordingly
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            toggleProgressbar(true)
            try {
                val authResult = firebaseAuth.signInAnonymously().await()
                // Sign in was successful, update UI State with the signed-in user's information
                Log.d(TAG, "signInAnonymously:success")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                Log.e(TAG, "signInAnonymously:failure", e)
                // TODO(thatfiredev): Display "Authentication failed" snackbar
                updateUiState(null)
            } finally {
                toggleProgressbar(false)
            }
        }
    }

    fun linkAccount(email: String, password: String) {
        // Make sure entered data is valid
        if (!validateEmailAndPassword(email, password)) {
            return
        }
        val credential = EmailAuthProvider.getCredential(email, password)

        viewModelScope.launch {
            toggleProgressbar(true)
            val firebaseUser = firebaseAuth.currentUser!!

            try {
                val authResult = firebaseUser.linkWithCredential(credential).await()
                // Account Link was successful, update UI State with the signed-in user's information
                Log.d(TAG, "linkWithCredential:success")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                Log.e(TAG, "linkWithCredential:failure", e)
                // TODO(thatfiredev): Display "Authentication failed" snackbar
                updateUiState(null)
            } finally {
                toggleProgressbar(false)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        updateUiState(null)
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user == null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    userId = "Signed Out",
                    userEmail = null,
                    isSignInEnabled = true,
                    isSignOutEnabled = false,
                    isLinkAccountEnabled = false,
                    isProgressBarVisible = false
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    userId = "User ID: ${user.uid}",
                    userEmail = "Email: ${user.email}",
                    isSignInEnabled = false,
                    isSignOutEnabled = true,
                    isLinkAccountEnabled = true,
                    isProgressBarVisible = false
                )
            }
        }
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Required.") }
            isValid = false
        } else {
            _uiState.update { it.copy(emailError = null) }
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Required.") }
            isValid = false
        } else {
            _uiState.update { it.copy(passwordError = null) }
        }

        return isValid
    }

    private fun toggleProgressbar(isVisible: Boolean) {
        _uiState.update { it.copy(isProgressBarVisible = isVisible) }
    }

    companion object {
        const val TAG = "AnonymousAuthViewModel"
    }
}