package com.google.firebase.quickstart.database.kotlin.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.database.kotlin.compose.flowcontrol.LogInStatus
import com.google.firebase.quickstart.database.kotlin.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthProviderViewModel(
    val auth: FirebaseAuth
) : ViewModel() {

    private val _signUpFlow = MutableStateFlow<LogInStatus<String>?>(null)
    private val signInFlow = MutableStateFlow<LogInStatus<String>?>(null)
    val loginFlow: StateFlow<LogInStatus<String>?> = signInFlow
    val signUpFlow: StateFlow<LogInStatus<String>?> = _signUpFlow

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get instance.
                val auth = Firebase.auth
                return AuthProviderViewModel(auth) as T
            }
        }
    }

    fun validateForm(email: String, password: String): Boolean {
        var result = true
        if (email.isEmpty()) {
            result = false
        } else {
        }

        if (password.isEmpty()) {
            result = false
        } else {
        }

        return result
    }


    fun signIn(email: String, password: String, database: FirebaseDatabase) = viewModelScope.launch {

        signInFlow.value = LogInStatus.InitState

        if (!validateForm(email, password)) {
            return@launch
        }

        try {
            auth.signInWithEmailAndPassword(email, password).await()
            onAuthSuccess(auth.currentUser!!, database)
            signInFlow.value = LogInStatus.Success()
        } catch (e: Exception) {
            signInFlow.value = LogInStatus.Failure(e)
        }
    }


    fun signUp(email: String, password: String, database: FirebaseDatabase) = viewModelScope.launch {

        if (!validateForm(email, password)) {
            _signUpFlow.value = LogInStatus.Failure(Exception())
            return@launch
        }

        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            onAuthSuccess(auth.currentUser!!, database)
            _signUpFlow.value = LogInStatus.Success()
        } catch (e: Exception) {
            _signUpFlow.value = LogInStatus.Failure(e)
        }
    }

    fun onAuthSuccess(user: FirebaseUser, database: FirebaseDatabase) {
        val username = usernameFromEmail(user.email!!)

        writeNewUser(user.uid, username, user.email, database)
    }

    fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

    fun writeNewUser(userId: String, name: String, email: String?, database: FirebaseDatabase) {
        val user = User(name, email)
        // database.child("users").child(userId).setValue(user)
        database.reference.child("users").child(userId).setValue(user)
    }
}