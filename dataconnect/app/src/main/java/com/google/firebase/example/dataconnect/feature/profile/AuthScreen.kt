package com.google.firebase.example.dataconnect.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    onSignUp: (email: String, password: String, displayName: String) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Name") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (isSignUp) {
                onSignUp(email, password, displayName)
            } else {
                onSignIn(email, password)
            }
        }) {
            Text(
                text = if (isSignUp) {
                    "Sign up"
                } else {
                    "Sign in"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSignUp) {
                "Already have an account?"
            } else {
                "Don't have an account?"
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            isSignUp = !isSignUp
        }) {
            Text(
                text = if (isSignUp) {
                    "Sign in"
                } else {
                    "Sign up"
                }
            )
        }
    }
}