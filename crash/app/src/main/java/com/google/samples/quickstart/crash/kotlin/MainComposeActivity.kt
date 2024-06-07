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

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.samples.quickstart.crash.R
import com.google.samples.quickstart.crash.kotlin.ui.theme.CrashTheme
import kotlinx.coroutines.launch

class MainComposeActivity : ComponentActivity() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var customKeySamples: CustomKeySamples
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Taken from original MainActivity.kt
        customKeySamples = CustomKeySamples(applicationContext)
        customKeySamples.setSampleCustomKeys()
        customKeySamples.updateAndTrackNetworkState()

        crashlytics = Firebase.crashlytics

        // Log the onCreate event, this will also be printed in logcat
        crashlytics.log("onCreate with jetpack compose")

        // Add some custom values and identifiers to be included in crash reports
        crashlytics.setCustomKeys {
            key("MeaningOfLife", 42)
            key("LastUIAction", "Test value")
        }
        crashlytics.setUserId("123456789")

        // Report a non-fatal exception, for demonstration purposes
        crashlytics.recordException(Exception("Non-fatal exception: something went wrong!"))

        setContent {
            CrashTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppView(crashlytics)
                }
            }
        }
    }
}

@Composable
fun MainAppView(crashlytics: FirebaseCrashlytics) {

    val scaffoldState = rememberScaffoldState() // this contains the `SnackbarHostState`
    val checkedState = remember { mutableStateOf(true) }

    // Log that the Activity was created.
    // [START crashlytics_log_event]
    crashlytics.log("Activity created")
    // [END crashlytics_log_event]

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

                        // crashlytics Functionality
                        crashlytics.log("Crash button clicked using jetpack compose.")
                        println("CRASHHHH")

                        if (checkedState.value) {
                            try {
                                throw NullPointerException()
                            } catch (ex: NullPointerException) {
                                // [START crashlytics_log_and_report]
                                crashlytics.log("NPE caught!")
                                crashlytics.recordException(ex)
                                // [END crashlytics_log_and_report]
                            }
                        } else {
                            throw NullPointerException()
                        }


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
