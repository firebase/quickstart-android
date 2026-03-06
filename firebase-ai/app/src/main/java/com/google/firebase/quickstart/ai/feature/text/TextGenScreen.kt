package com.google.firebase.quickstart.ai.feature.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun TextGenScreen(
    textGenViewModel: TextGenViewModel = viewModel<TextGenViewModel>()
) {
    var textPrompt by rememberSaveable { mutableStateOf(textGenViewModel.initialPrompt) }
    val errorMessage by textGenViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by textGenViewModel.isLoading.collectAsStateWithLifecycle()
    val generatedText by textGenViewModel.generatedText.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            OutlinedTextField(
                value = textPrompt,
                label = { Text("Prompt") },
                placeholder = { Text("Enter text to generate") },
                onValueChange = { textPrompt = it },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            Row() {
                TextButton(
                    onClick = {
                        if (textGenViewModel.allowEmptyPrompt || textPrompt.isNotBlank()) {
                            textGenViewModel.generate(textPrompt)
                        }
                    },
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Text("Generate")
                }
            }

        }

        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator()
            }
        }
        errorMessage?.let {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
        generatedText?.let {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
    }
}
