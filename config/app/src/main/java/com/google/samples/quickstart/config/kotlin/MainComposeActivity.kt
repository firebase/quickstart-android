package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.samples.quickstart.config.R
import com.google.samples.quickstart.config.kotlin.ui.theme.ConfigTheme


class MainComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize config and set starting text
        val remoteConfig = initializeConfig()

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

    private fun initializeConfig() : FirebaseRemoteConfig {
        // Get Remote Config instance.
        // [START get_remote_config_instance]
        val remoteConfig = Firebase.remoteConfig
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // [END set_default_values]
        return remoteConfig
    }
}


@Composable
fun MainAppView(
    modifier: Modifier = Modifier,
    remoteConfigViewModel: RemoteConfigViewModel = viewModel()
){
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally){
        AppNameBanner()
        Spacer(modifier = Modifier.height(24.dp))

        Image(painter = painterResource(R.drawable.firebase_lockup_400), contentDescription = "")

        // Text displayed
        val remoteConfigDisplayText by remoteConfigViewModel.welcomeMessage.collectAsState()
        Text(
            text = remoteConfigDisplayText,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(160.dp))

        // Button to fetch remote welcome
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorAccent)),
            onClick = { remoteConfigViewModel.fetchConfig() } // Calls function from MainComposeActivity to change display text
        ) {
            Text(
                text = stringResource(R.string.fetch_remote_welcome_message),
                fontSize = 20.sp
            )
        }
    }

}

@Composable
fun AppNameBanner(modifier: Modifier = Modifier){
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
