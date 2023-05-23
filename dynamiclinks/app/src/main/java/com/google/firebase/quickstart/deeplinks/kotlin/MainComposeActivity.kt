package com.google.firebase.quickstart.deeplinks.kotlin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.deeplinks.R
import com.google.firebase.quickstart.deeplinks.kotlin.ui.theme.DynamicLinksTheme
import kotlinx.coroutines.channels.Channel

private const val DEEP_LINK_URL = "https://www.youtube.com/deeplinks"

class MainComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicLinksTheme {
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
fun MainAppView(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    dynamicLinksViewModel: DynamicLinksViewModel = viewModel(factory = DynamicLinksViewModel.Factory)
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val intent = activity?.intent
    val uriPrefix = stringResource(R.string.dynamic_links_uri_prefix)
    var newDeepLink by remember { mutableStateOf("") }
    var openDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Configure Firebase Dynamic Links when the screen is created
            if (event == Lifecycle.Event.ON_CREATE) {
                // Validate that the developer has set the app code.
                dynamicLinksViewModel.validateAppCode(uriPrefix)

                newDeepLink = dynamicLinksViewModel.buildDeepLink(uriPrefix, Uri.parse(DEEP_LINK_URL), 0).toString()

                intent?.let {
                    dynamicLinksViewModel.getDynamicLink(it)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val channel = remember { Channel<Int>(Channel.Factory.CONFLATED) }
    val scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState)

    // Checks if a valid uri has been updated, show only once
    LaunchedEffect(key1 = true) {
        dynamicLinksViewModel.validUriPrefix.collect { flag ->
            if (!flag) {
                openDialog = true
            }
        }
    }

    // Checks if a deep link is used to open app
    LaunchedEffect(key1 = channel) {
        dynamicLinksViewModel.deepLink.collect { deepLink ->
            if (deepLink.isNotEmpty()) {
                snackbarHostState.showSnackbar("Found deep link!")
            }
        }
    }
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
        },
        content = { it ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                MainContent(
                    _openDialog = openDialog,
                    _linkReceiveTextView = dynamicLinksViewModel.deepLink.collectAsState(),
                    _shortLinkTextView = dynamicLinksViewModel.shortLink.collectAsState(),
                    newDeepLink = newDeepLink,
                    buildDeepLink = {
                        dynamicLinksViewModel.buildDeepLink(uriPrefix, Uri.parse(DEEP_LINK_URL), 0).toString()
                    },
                    buildShortLinkFromParams = {
                        dynamicLinksViewModel.buildShortLinkFromParams(uriPrefix, it, 0)
                    }
                )
            }
        }
    )
}

@Composable
fun MainContent(
    _openDialog: Boolean = false,
    _linkReceiveTextView: State<String> = mutableStateOf(""),
    _shortLinkTextView: State<String> = mutableStateOf(""),
    newDeepLink: String = "",
    buildDeepLink: () -> String = { "" },
    buildShortLinkFromParams: (Uri) -> Unit = {},
){
    val context = LocalContext.current
    var openDialog by remember { mutableStateOf(_openDialog) }
    val linkReceiveTextView by _linkReceiveTextView
    val shortLinkTextView by _shortLinkTextView

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        if(openDialog) {
            AlertDialog(
                onDismissRequest = { openDialog = false },
                title = { Text("Invalid Configuration") },
                text = { Text("Please set your Dynamic Links domain in app/build.gradle") },
                confirmButton = {
                    Button(onClick = {
                        openDialog = false
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }

        Image(
            painter = painterResource(R.drawable.firebase_lockup_400),
            contentDescription = "",
            modifier = Modifier.fillMaxWidth(),
            alignment = Alignment.Center
        )

        Text(
            text = stringResource(R.string.title_receive),
            fontSize = 20.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = linkReceiveTextView.ifEmpty {
                stringResource(R.string.msg_no_deep_link)
            },
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = stringResource(R.string.dynamic_link),
            fontSize = 20.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 32.dp)
        )

        Text(
            text = buildDeepLink().ifEmpty { "https://abc.xyz/foo" },
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                shareDeepLink(context, newDeepLink)
            }
        ) {
            Text(
                text = stringResource(R.string.share_dynamic_link).uppercase(),
                color = Color.White,
            )
        }

        Text(
            text = stringResource(R.string.short_dynamic_link),
            fontSize = 20.sp,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 32.dp)
        )

        Text(
            text = shortLinkTextView.ifEmpty {
                "https://abc.xyz/foo"
            },
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                val deepLink = Uri.parse(DEEP_LINK_URL)
                buildShortLinkFromParams(deepLink)
            }
        ) {
            Text(
                text = stringResource(R.string.generate_short_link).uppercase(),
                color = Color.White
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorPrimary)),
            onClick = {
                shareDeepLink(context, shortLinkTextView)
            }
        ) {
            Text(
                text = stringResource(R.string.share_short_link).uppercase(),
                color = Color.White
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MainContentPreview(){
    DynamicLinksTheme {
        MainContent()
    }
}

fun shareDeepLink(context: Context, deepLink: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link")
    intent.putExtra(Intent.EXTRA_TEXT, deepLink)

    context.startActivity(intent)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}