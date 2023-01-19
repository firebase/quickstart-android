package com.google.samples.quickstart.config.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.samples.quickstart.config.R
import com.google.samples.quickstart.config.kotlin.ui.theme.ConfigTheme
//import com.google.samples.quickstart.config.ui.theme.ConfigTheme


class MainComposeActivity : ComponentActivity() {
    private var startingText: String = "Fetching Config..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfigTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppView(remoteConfigDisplayText = startingText)
                }
            }
        }
    }
}


@Composable
fun MainAppView(modifier: Modifier = Modifier, remoteConfigDisplayText: String){
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally){
        AppNameBanner()
        Spacer(modifier = Modifier.height(24.dp))

        Image(painter = painterResource(R.drawable.firebase_lockup_400), contentDescription = "")
//        Spacer(modifier = Modifier.height(16.dp))

        ConfigText(remoteConfigDisplayText = remoteConfigDisplayText)
        Spacer(modifier = Modifier.height(160.dp))

        ConfigButton()
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

@Composable
fun ConfigText(modifier: Modifier = Modifier, remoteConfigDisplayText: String){
    Text(
        text = remoteConfigDisplayText,
        fontSize = 16.sp
    )
}

@Composable
fun ConfigButton(modifier: Modifier = Modifier){
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorAccent)),
        onClick = {  }    //lambda for onClick action
    ) {
        Text(
            text = stringResource(R.string.fetch_remote_welcome_message),
            fontSize = 20.sp
        )
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ConfigTheme {
        Greeting("Android")
    }
}