package com.google.firebase.quickstart.auth.kotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PasswordlessViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    private var pendingEmail: String = ""
    private var emailLink: String = ""

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var status: String? = null,
        var userEmail: String? = null,
        var isSignInEnabled: Boolean = true,
        var isProgressBarVisible: Boolean = false,
        var emailError: String? = null
    )

    init {
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)
    }

    fun sendSignInLink(email: String, appPackageName: String) {
        val settings = actionCodeSettings {
            setAndroidPackageName(
                appPackageName,
                false, null/* minimum app version */)/* install if not available? */
            handleCodeInApp = true
            url = "https://kotlin.auth.example.com/emailSignInLink"
        }

        toggleProgressbar(isVisible = true)

        viewModelScope.launch {
            try {
                firebaseAuth.sendSignInLinkToEmail(email, settings).await()
                Log.d(TAG, "Link sent")
                // TODO(thatfiredev): showSnackbar("Sign-in link sent!")

                pendingEmail = email
                _uiState.update { it.copy(status = "Link sent, check your email to continue.") }
            } catch (e: Exception) {
                Log.w(TAG, "Could not send link", e)
                // TODO(thatfiredev): showSnackbar("Failed to send link.")
                if (e is FirebaseAuthInvalidCredentialsException) {
                    _uiState.update { it.copy(emailError = "Invalid email address.") }
                }
            } finally {
                toggleProgressbar(isVisible = false)
            }
        }
    }

    fun isSignInWithEmailLink(link: String): Boolean {
        if (firebaseAuth.isSignInWithEmailLink(link)) {
            emailLink = link
            return true
        }
        return false
    }

    fun signInWithEmailLink(email: String) {
        toggleProgressbar(isVisible = true)

        viewModelScope.launch {
            try {
                Log.d(TAG, "signInWithLink:$emailLink")
                val authResult = firebaseAuth.signInWithEmailLink(email, emailLink).await()
                Log.d(TAG, "signInWithEmailLink:success")
                _uiState.update { it.copy(userEmail = null) }
                updateUiState(authResult.user)
            } catch (e: Exception) {
                Log.w(TAG, "signInWithEmailLink:failure", e)
                updateUiState(null)

                if (e is FirebaseAuthActionCodeException) {
                    // TODO(thatfiredev): showSnackbar("Invalid or expired sign-in link.")
                }
            } finally {
                toggleProgressbar(isVisible = false)
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        updateUiState(null)
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user != null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Email User: ${user.email} (verified: ${user.isEmailVerified})",
                    userEmail = "Email: ${user.email}",
                    isSignInEnabled = false,
                    isProgressBarVisible = false
                )
            }
        } else {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "Signed Out",
                    userEmail = null,
                    isSignInEnabled = true,
                    isProgressBarVisible = false
                )
            }
        }
    }

    private fun toggleProgressbar(isVisible: Boolean) {
        _uiState.update { it.copy(isProgressBarVisible = isVisible) }
    }

    companion object {
        const val TAG = "PasswordlessViewModel"
    }
}