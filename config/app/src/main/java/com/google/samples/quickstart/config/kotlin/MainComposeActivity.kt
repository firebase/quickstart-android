package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.samples.quickstart.config.R
import com.google.samples.quickstart.config.kotlin.ui.theme.ConfigTheme

class MainComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfigTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppView()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppView(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    remoteConfigViewModel: RemoteConfigViewModel = viewModel(factory = RemoteConfigViewModel.Factory)
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Configure Firebase Remote Config when the screen is created
            if (event == Lifecycle.Event.ON_CREATE) {
                remoteConfigViewModel.enableDeveloperMode()
                remoteConfigViewModel.setDefaultValues(R.xml.remote_config_defaults)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(topBar = {
        AppNameBanner()
    }, content = {
        Column(modifier = Modifier.padding(it).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(painter = painterResource(R.drawable.firebase_lockup_400), contentDescription = "")

            // Text displayed
            val remoteConfigDisplayText by remoteConfigViewModel.welcomeMessage.collectAsState()

            val allCaps by remoteConfigViewModel.allCaps.collectAsState()

            Text(
                text = if (allCaps) {
                    remoteConfigDisplayText.uppercase()
                } else {
                    remoteConfigDisplayText
                },
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(160.dp))

            // Button to fetch remote welcome
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorAccent)),
                onClick = {
                    // Fetch config and update the display text
                    remoteConfigViewModel.fetchRemoteConfig()
                }
            ) {
                Text(
                    text = stringResource(R.string.fetch_remote_welcome_message),
                    fontSize = 20.sp
                )
            }
        }
    })


}

@Composable
fun AppNameBanner(modifier: Modifier = Modifier) {
    TopAppBar(
        backgroundColor = colorResource(R.color.colorPrimary)
    ) {
        androidx.compose.material.Text(
            text = stringResource(R.string.app_name),
            style = androidx.compose.material.MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
            color = Color.White
        )
    }
}
