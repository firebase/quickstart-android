package com.google.firebase.quickstart.database.kotlin.compose

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Screen
import com.google.firebase.quickstart.database.kotlin.compose.flowcontrol.LogInStatus
import com.google.firebase.quickstart.database.kotlin.theme.RealtimeDatabaseTheme

@Composable
fun LoginPage(rootNavController: NavHostController) {
    RealtimeDatabaseTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
        ) {
            Authenticate(rootNavController)
        }
    }
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Authenticate(
    rootNavController: NavHostController,
    authProviderViewModel: AuthProviderViewModel = viewModel(factory = AuthProviderViewModel.Factory),
    databaseProviderViewModel: DatabaseProviderViewModel = viewModel(factory = DatabaseProviderViewModel.Factory)
) {
    val database = databaseProviderViewModel.database
    val loginFlow = authProviderViewModel.loginFlow.collectAsState()
    val signUpFlow = authProviderViewModel.signUpFlow.collectAsState()

    ComposableLifecycle { source, event ->
        if (event == Lifecycle.Event.ON_START) {
            authProviderViewModel.auth.currentUser?.let {
                authProviderViewModel.onAuthSuccess(it, database)
                rootNavController.navigate(Screen.HomeScreen.route)
            }
        }
    }

    val email = remember { mutableStateOf<String>("") }
    val password = remember { mutableStateOf<String>("") }


    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        it
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.firebase_lockup_400),
                contentDescription = "Firebase logo",
                contentScale = ContentScale.Crop,
            )
            Row()
            {
                OutlinedTextField(
                    modifier = Modifier.width(150.dp),
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("email") }
                )
                OutlinedTextField(
                    modifier = Modifier.width(150.dp),
                    label = { Text(text = "password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    value = password.value,
                    onValueChange = {
                        password.value = it
                    },
                )
            }
            Row()
            {
                Button(
                    onClick = {
                        authProviderViewModel.signIn(email.value, password.value, database)
                    },
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("SignIn")
                }
                login(loginFlow, rootNavController)
                Button(
                    onClick = {
                        authProviderViewModel.signUp(email.value, password.value, database)
                    },
                    modifier = Modifier.width(150.dp)
                ) {
                    Text("SignUp")
                }
                signUp(signUpFlow,rootNavController)
            }
        }
    }
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun login(
    logInStatus: State<LogInStatus<String>?>,
    rootNavController: NavHostController,
) {

    logInStatus?.value?.let { status ->
        when (status) {
            is LogInStatus.Failure -> {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Log in failed: " + status.exception.message, Toast.LENGTH_LONG).show()
                }
            }
            is LogInStatus.Success -> {
                LaunchedEffect(Unit) {
                    rootNavController.navigate(Screen.HomeScreen.route)
                }
            }
            is LogInStatus.InitState -> {}
        }

    }
}

@Composable
fun signUp(
    signUpStatus: State<LogInStatus<String>?>,
    rootNavController: NavHostController,
) {

    signUpStatus?.value?.let { status ->
        when (status) {
            is LogInStatus.Failure -> {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Signup failed: " + status.exception.message, Toast.LENGTH_SHORT).show()
                }
            }
            is LogInStatus.Success -> {
                LaunchedEffect(Unit) {
                    rootNavController.navigate(Screen.HomeScreen.route)
                }
            }
            is LogInStatus.InitState -> {}
        }

    }
}