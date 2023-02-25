package com.google.samples.quickstart.crash.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.samples.quickstart.crash.R
import com.google.samples.quickstart.crash.kotlin.ui.theme.CrashTheme
import kotlinx.coroutines.launch

class MainComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrashTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppView()
                }
            }
        }
    }
}

@Composable
fun MainAppView() {

    val scaffoldState = rememberScaffoldState() // this contains the `SnackbarHostState`
    val checkedState = remember { mutableStateOf(true) }


    Scaffold(
        scaffoldState = scaffoldState,
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
        }, content = {
            Column(modifier = Modifier.padding(it).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))

                Image(painter = painterResource(R.drawable.firebase_lockup_400), contentDescription = "")

                Spacer(modifier = Modifier.height(50.dp))

                // Button to fetch remote welcome
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorAccent)),
                    onClick = {

                    }
                ) {
                    Text(
                        text = "CAUSE CRASH",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Checkbox(
                        checked = checkedState.value,
                        onCheckedChange = { checkedState.value = it }
                    )
                    Text(text = "Catch Crash", fontSize = 20.sp ,modifier = Modifier.padding(8.dp))
                }

            }
        })
}

