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

class EmailPasswordViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var userId: String? = null,
        var userEmail: String? = null,
        var isSignInEnabled: Boolean = true,
        var isCreateAccountEnabled: Boolean = false,
        var isVerifyEmailVisible: Boolean = false,
        var isReloadEnabled: Boolean = false,
        var isProgressBarVisible: Boolean = false,
        var emailError: String? = null,
        var passwordError: String?= null,
    )

    init {
        // Check if there's a user signed in and update UI State accordingly
        val firebaseUser = firebaseAuth.currentUser
        firebaseUser?.let { reload() }
    }

    fun signIn(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) {
            return
        }

        Log.d(TAG, "signIn:$email")

        viewModelScope.launch {
            toggleProgressbar(isVisible = true)
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithEmail:success")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                // If sign in fails, display a message to the user.
                Log.e(TAG, "signInWithEmail:failure", e)
                // TODO(thatfiredev): Show snackbar
                updateUiState(null)
                // TODO(thatfiredev): Check for multifactor failure once firebase/quickstart-android#1443 is fixed

                _uiState.update { it.copy(userId = "Authentication failed") }
            } finally {
                toggleProgressbar(isVisible = false)
            }
        }
    }

    fun createAccount(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) {
            return
        }

        Log.d(TAG, "createAccount:$email")

        viewModelScope.launch {
            toggleProgressbar(isVisible = true)
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                // If account creation fails, display a message to the user.
                Log.e(TAG, "createUserWithEmail:failure", e)
                // TODO(thatfiredev): Show snackbar
                updateUiState(null)
            } finally {
                toggleProgressbar(isVisible = false)
            }
        }
    }

    fun sendEmailVerification() {
        // Disable button
        _uiState.update { it.copy(isVerifyEmailVisible = false) }

        viewModelScope.launch {
            try {
                // Send verification email
                firebaseAuth.currentUser!!.sendEmailVerification().await()

                // TODO: Show snackbar "Verification email sent to ${user.email} "
            } catch (e: Exception) {
                Log.e(TAG, "sendEmailVerification", e)
                // TODO: Show snackbar "Failed to send verification email."
            } finally {
                // Re-enable button
                _uiState.update { it.copy(isVerifyEmailVisible = true) }
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            try {
                firebaseAuth.currentUser!!.reload().await()
                updateUiState(firebaseAuth.currentUser)
                // TODO(thatfiredev): Show "Reload successful" snackbar
            } catch (e: Exception) {
                Log.e(TAG, "reload", e)
                // TODO(thatfiredev): Show "Failed to reload user." snackbar
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
                    isCreateAccountEnabled = false,
                    isProgressBarVisible = false
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    userId = "Firebase UID: ${user.uid}",
                    userEmail = "Email User: ${user.email} (verified: ${user.isEmailVerified})",
                    isSignInEnabled = false,
                    isVerifyEmailVisible = !user.isEmailVerified,
                    isCreateAccountEnabled = true,
                    isProgressBarVisible = false
                )
            }
        }
    }

    private fun toggleProgressbar(isVisible: Boolean) {
        _uiState.update { it.copy(isProgressBarVisible = isVisible) }
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

    companion object {
        private const val TAG = "EmailPasswordViewModel"
    }
}