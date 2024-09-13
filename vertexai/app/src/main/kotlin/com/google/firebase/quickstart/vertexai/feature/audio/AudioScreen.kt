/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.vertexai.feature.audio

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.vertexai.GenerativeViewModelFactory
import com.google.firebase.quickstart.vertexai.R
import kotlinx.coroutines.launch

@Composable
internal fun AudioRoute(
    viewModel: AudioViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val audioUiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    AudioScreen(
        viewModel.audioRecorder,
        uiState = audioUiState,
        onReasonClicked = { inputText, audioData ->
            coroutineScope.launch { viewModel.reason(inputText, audioData) }
        },
    )
}

@Composable
fun AudioScreen(
    audioRecorder: AudioRecorder = AudioRecorder(),
    uiState: AudioUiState = AudioUiState.Loading,
    onReasonClicked: (String, ByteArray) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current

    var userQuestion by rememberSaveable { mutableStateOf("") }
    var recordGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED
        )
    }
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var audioData by rememberSaveable { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        recordGranted = isGranted
    }

    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(vertical = 16.dp)) {
                if (!recordGranted) {
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                            Text(stringResource(R.string.grant_record))
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                audioData = audioRecorder.stopRecording()
                                isRecording = false
                            } else if (audioData == null) {
                                audioRecorder.startRecording(context)
                                isRecording = true
                            } else {
                                audioData = null
                            }
                        },
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .align(Alignment.CenterVertically),
                    ) {
                        Icon(
                            imageVector = if (isRecording) {
                                ImageVector.vectorResource(R.drawable.stop)
                            } else if (audioData == null) {
                                ImageVector.vectorResource(R.drawable.mic)
                            } else {
                                Icons.Filled.Delete
                            },
                            contentDescription = stringResource(
                                if (isRecording) {
                                    R.string.stop_recording
                                } else if (audioData == null) {
                                    R.string.start_recording
                                } else {
                                    R.string.delete_clip
                                }
                            ),
                        )
                    }
                    OutlinedTextField(
                        value = userQuestion,
                        label = { Text(stringResource(R.string.audio_label)) },
                        placeholder = { Text(stringResource(R.string.audio_hint)) },
                        onValueChange = { userQuestion = it },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    )
                    TextButton(
                        onClick = {
                            if (audioData != null) onReasonClicked(userQuestion, audioData!!)
                        },
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .align(Alignment.CenterVertically),
                    ) {
                        Text(
                            stringResource(R.string.action_go),
                            color = if (audioData == null) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
        when (uiState) {
            AudioUiState.Initial -> {
                // Nothing is shown
            }

            AudioUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    CircularProgressIndicator()
                }
            }

            is AudioUiState.Success -> {
                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Person Icon",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .requiredSize(36.dp)
                                .drawBehind { drawCircle(color = Color.White) },
                        )
                        Text(
                            text = uiState.outputText,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }

            is AudioUiState.Error -> {
                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(all = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun AudioScreenPreview() {
    AudioScreen()
}
