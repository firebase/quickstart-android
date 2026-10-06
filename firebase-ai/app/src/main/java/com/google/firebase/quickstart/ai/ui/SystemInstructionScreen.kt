package com.google.firebase.quickstart.ai.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.quickstart.ai.feature.hybrid.GenerationMethod
import com.google.firebase.quickstart.ai.feature.hybrid.SystemInstructionCase
import com.google.firebase.quickstart.ai.feature.hybrid.SystemInstructionViewModel

@OptIn(PublicPreviewAPI::class)
@Composable
fun SystemInstructionScreen(
    viewModel: SystemInstructionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var prompt by rememberSaveable { mutableStateOf("Tell me a fun fact about space.") }
    var primaryInstruction by rememberSaveable {
        mutableStateOf("Respond like a 17th-century pirate in 1-2 short sentences.")
    }
    var secondaryInstruction by rememberSaveable {
        mutableStateOf("Always end your response with the phrase 'Arrr, matey!'")
    }
    var selectedCase by rememberSaveable { mutableStateOf(SystemInstructionCase.SINGLE_TEXT) }
    var selectedMode by rememberSaveable { mutableStateOf(InferenceMode.ONLY_ON_DEVICE) }
    var selectedMethod by rememberSaveable { mutableStateOf(GenerationMethod.UNARY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "On-Device Model Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = uiState.modelStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Text(
            text = "1. System Instruction Case:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SystemInstructionCase.entries.forEach { case ->
                FilterChip(
                    selected = selectedCase == case,
                    onClick = { selectedCase = case },
                    label = { Text(case.label) }
                )
            }
        }

        if (selectedCase != SystemInstructionCase.NONE) {
            OutlinedTextField(
                value = primaryInstruction,
                onValueChange = { primaryInstruction = it },
                label = { Text("System Instruction (TextPart 1)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        if (selectedCase == SystemInstructionCase.MULTIPLE_TEXT) {
            OutlinedTextField(
                value = secondaryInstruction,
                onValueChange = { secondaryInstruction = it },
                label = { Text("System Instruction (TextPart 2 - Concatenated)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("User Prompt") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Text(
            text = "2. Inference Mode:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                InferenceMode.ONLY_ON_DEVICE to "ONLY_ON_DEVICE",
                InferenceMode.PREFER_ON_DEVICE to "PREFER_ON_DEVICE",
                InferenceMode.PREFER_IN_CLOUD to "PREFER_IN_CLOUD",
                InferenceMode.ONLY_IN_CLOUD to "ONLY_IN_CLOUD"
            )
            modes.forEach { (mode, label) ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    label = { Text(label) }
                )
            }
        }

        Text(
            text = "3. Generation Method:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenerationMethod.entries.forEach { method ->
                FilterChip(
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    label = { Text(method.label) }
                )
            }
        }

        Button(
            onClick = {
                viewModel.runTest(
                    promptText = prompt,
                    primaryInstruction = primaryInstruction,
                    secondaryInstruction = secondaryInstruction,
                    case = selectedCase,
                    mode = selectedMode,
                    method = selectedMethod
                )
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text("Run System Instruction Test")
        }

        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (uiState.responseText != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Response",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = uiState.inferenceSource ?: "UNKNOWN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = uiState.responseText!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Diagnostics & Logcat Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.logOutput,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
