package com.google.firebase.quickstart.auth.kotlin

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.ktx.Firebase
import java.util.ArrayList
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
        var isSignInVisible: Boolean = true,
        val providerNames: List<String> = ArrayList(PROVIDER_MAP.keys)
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

    fun signIn(activity: Activity, providerName: String) {
        // Could add custom scopes here
        val customScopes = listOf<String>()

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        val providerId = PROVIDER_MAP[providerName]!!

        val oAuthProvider = oAuthProvider(providerId) {
            scopes = customScopes
        }

        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.startActivityForSignInWithProvider(activity, oAuthProvider).await()
                Log.d(TAG, "activitySignIn:onSuccess:${authResult.user}")
                updateUiState(authResult.user)
            } catch (e: Exception) {
                Log.w(TAG, "activitySignIn:onFailure", e)
                // TODO(thatfiredev): Snackbar Sign in failed, see logs for details.
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
        private val PROVIDER_MAP = mapOf(
            "Apple" to "apple.com",
            "Microsoft" to "microsoft.com",
            "Yahoo" to "yahoo.com",
            "Twitter" to "twitter.com"
        )
    }
}