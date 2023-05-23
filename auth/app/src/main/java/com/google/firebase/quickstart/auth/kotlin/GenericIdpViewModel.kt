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

class GenericIdpViewModel(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    data class UiState(
        var status: String = "",
        var detail: String? = null,
        var isSignInVisible: Boolean = true
    )

    fun showSignedInUser() {
        // Check if user is signed in (non-null) and update UI accordingly.
        val firebaseUser = firebaseAuth.currentUser
        updateUiState(firebaseUser)

        // Look for a pending auth result
        val pendingAuthResult = firebaseAuth.pendingAuthResult
        if (pendingAuthResult != null) {
            viewModelScope.launch {
                try {
                    val authResult = pendingAuthResult.await()
                    Log.d(TAG, "checkPending:onSuccess:$authResult")
                    updateUiState(authResult.user)
                } catch (e: Exception) {
                    Log.w(TAG, "checkPending:onFailure", e)
                }
            }
        } else {
            Log.d(TAG, "checkPending: null")
        }
    }

    fun showSignedInUser(user: FirebaseUser) {
        updateUiState(user)
    }

    fun signOut() {
        firebaseAuth.signOut()
        updateUiState(null)
    }

    private fun updateUiState(user: FirebaseUser?) {
        if (user != null) {
            _uiState.update { currentUiState ->
                currentUiState.copy(
                    status = "User: ${user.displayName} ${user.email}",
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
        const val TAG = "GenericIdpViewModel"
    }
}