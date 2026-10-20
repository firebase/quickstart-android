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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.quickstart.ai.feature.hybrid.GenerationMethod
import com.google.firebase.quickstart.ai.feature.hybrid.InferenceModeOption
import com.google.firebase.quickstart.ai.feature.hybrid.OnDeviceModelOptionSelection
import com.google.firebase.quickstart.ai.feature.hybrid.ThinkingModeViewModel
import com.google.firebase.quickstart.ai.feature.hybrid.ThinkingOption

@OptIn(PublicPreviewAPI::class)
@Composable
fun ThinkingModeScreen(
    viewModel: ThinkingModeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var prompt by rememberSaveable {
        mutableStateOf(
            "A bat and a ball cost $1.10 in total. The bat costs $1.00 more than the ball. " +
                "How much does the ball cost? Explain step by step."
        )
    }
    var selectedThinkingOption by rememberSaveable { mutableStateOf(ThinkingOption.ENABLED) }
    var selectedModelOption by rememberSaveable { mutableStateOf(OnDeviceModelOptionSelection.DEFAULT) }
    var selectedMode by rememberSaveable { mutableStateOf(InferenceModeOption.ONLY_ON_DEVICE) }
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
            text = "1. Thinking Mode (enableThinking):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThinkingOption.entries.forEach { option ->
                FilterChip(
                    selected = selectedThinkingOption == option,
                    onClick = { selectedThinkingOption = option },
                    label = { Text(option.label) }
                )
            }
        }

        Text(
            text = "2. On-Device Model Option:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnDeviceModelOptionSelection.entries.forEach { modelOpt ->
                FilterChip(
                    selected = selectedModelOption == modelOpt,
                    onClick = {
                        selectedModelOption = modelOpt
                        viewModel.checkAndDownloadModel(modelOpt.option)
                    },
                    label = { Text(modelOpt.label) }
                )
            }
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("User Prompt") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Text(
            text = "3. Inference Mode:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InferenceModeOption.entries.forEach { modeOption ->
                FilterChip(
                    selected = selectedMode == modeOption,
                    onClick = { selectedMode = modeOption },
                    label = { Text(modeOption.label) }
                )
            }
        }

        Text(
            text = "4. Generation Method:",
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
                    thinkingOption = selectedThinkingOption,
                    modelOptionSelection = selectedModelOption,
                    mode = selectedMode.mode,
                    method = selectedMethod
                )
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text("Run Thinking Mode Test")
        }

        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Error / Exception",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        val thoughtSummary = uiState.thoughtSummary
        if (thoughtSummary != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Thought Summary (response.thoughtSummary)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = thoughtSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        val responseText = uiState.responseText
        if (responseText != null) {
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
                            text = "Response (response.text)",
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
                        text = responseText,
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
