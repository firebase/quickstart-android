package com.google.firebase.quickstart.fcm.kotlin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.fcm.R
import com.google.firebase.quickstart.fcm.kotlin.data.SubscriptionState
import com.google.firebase.quickstart.fcm.kotlin.ui.theme.FirebaseMessagingTheme
import kotlinx.coroutines.launch

class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseMessagingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    fcmViewModel: FirebaseMessagingViewModel = viewModel(factory = FirebaseMessagingViewModel.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val activity = context.findActivity()
    val intent = activity?.intent

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        var message = context.getString(R.string.msg_permission_granted)
        if (!isGranted) {
            message = context.getString(R.string.msg_permission_failed)
        }

        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                //Create Notification Channel
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    fcmViewModel.setNotificationChannel(
                        context,
                        context.getString(R.string.default_notification_channel_id),
                        context.getString(R.string.default_notification_channel_name)
                    )
                }

                intent?.let { intentData ->
                    fcmViewModel.logNotificationData(intentData)
                }

                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.msg_setup_readme_instructions))
                }

                fcmViewModel.askNotificationPermission(context, requestPermissionLauncher)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(fcmViewModel.token) {
        fcmViewModel.token.collect {
            if(it.isNotEmpty()) {
                snackbarHostState.showSnackbar(context.getString(R.string.msg_token_fmt, it))
            }
        }
    }

    LaunchedEffect(fcmViewModel.subscriptionState) {
        fcmViewModel.subscriptionState.collect { state ->
            when (state) {
                SubscriptionState.Success -> { snackbarHostState.showSnackbar(context.getString(R.string.msg_subscribed)) }
                SubscriptionState.Failed -> { snackbarHostState.showSnackbar(context.getString(R.string.msg_subscribe_failed)) }
                SubscriptionState.Loading -> { }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                backgroundColor = colorResource(R.color.colorPrimary)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                    color = Color.White
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MainContent(fcmViewModel)
            }
        }
    )
}

@Composable
fun MainContent(
    fcmViewModel: FirebaseMessagingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.firebase_lockup_400),
            contentDescription = "Firebase logo",
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.quickstart_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(top = 16.dp, start=8.dp, end=8.dp, bottom=16.dp)
        )
        Button(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                fcmViewModel.subscribeToTopic("weather")
            }
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(R.string.subscribe_to_weather).uppercase(),
                color = Color.White,
            )
        }
        Button(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                fcmViewModel.getToken()
            }
        ) {
            Text(
                text = stringResource(R.string.log_token).uppercase(),
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppViewPreview() {
    FirebaseMessagingTheme {
        MainScreen()
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}