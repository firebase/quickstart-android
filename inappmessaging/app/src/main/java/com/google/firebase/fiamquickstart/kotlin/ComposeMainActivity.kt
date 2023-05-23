package com.google.firebase.fiamquickstart.kotlin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.fiamquickstart.R
import com.google.firebase.fiamquickstart.kotlin.ui.theme.InAppMessagingTheme
import kotlinx.coroutines.launch

class ComposeMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InAppMessagingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppView()
                }
            }
        }
    }

    companion object {
        private const val TAG = "Compose-FIAM-Quickstart"
    }
}

@Composable
fun MainAppView(
    inAppMessagingViewModel: InAppMessagingViewModel = viewModel(factory = InAppMessagingViewModel.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState)

    Scaffold(
        scaffoldState = scaffoldState,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                MainContent(
                    _installationsId = inAppMessagingViewModel.installationsId.collectAsState(""),
                    triggerEvent = {
                        inAppMessagingViewModel.triggerEvent()
                        scope.launch {
                            snackbarHostState.showSnackbar("engagement_party' event triggered!")
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun MainContent(
    _installationsId: State<String> = mutableStateOf(""),
    triggerEvent: ()-> Unit = {}
) {
    val installationsId by remember { _installationsId }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(R.drawable.firebase_lockup_400),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            alignment = Alignment.Center
        )

        Text(
            text = stringResource(R.string.textview_text),
            fontSize = 18.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )

        Text(
            text = stringResource(R.string.warning_fresh_install),
            fontSize = 18.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
        )

        Text(
            text = if(installationsId.isEmpty()){
                "Device Instance ID: 1234"
            } else {
                stringResource(R.string.installation_id_fmt, installationsId)
            },
            fontSize = 18.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                triggerEvent()
            }
        ) {
            Text(
                text = stringResource(R.string.button_text).uppercase(),
                color = Color.White
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
fun MainContentPreview(){
    InAppMessagingTheme {
        MainContent()
    }
}
