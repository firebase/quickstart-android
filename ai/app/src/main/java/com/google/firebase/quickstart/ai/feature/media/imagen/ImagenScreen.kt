package com.google.firebase.quickstart.ai.feature.media.imagen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable

@Serializable
class ImagenRoute(val sampleId: String)

@Composable
fun ImagenScreen(
    imagenViewModel: ImagenViewModel = viewModel<ImagenViewModel>()
) {
    var imagenPrompt by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            OutlinedTextField(
                value = imagenPrompt,
                label = { Text("Prompt") },
                placeholder = { Text("Enter text to generate image") },
                onValueChange = { imagenPrompt = it },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            TextButton(
                onClick = {
                    if (imagenPrompt.isNotBlank()) {
//                        onImagenClicked(imagenPrompt)
                    }
                },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .align(Alignment.End)
            ) {
                Text("Generate")
            }
        }

//      // TODO: Re-add when the viewModel is ready
//        when (uiState) {
//            ImagenUiState.Initial -> {
//                // Nothing is shown
//            }
//
//            ImagenUiState.Loading -> {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .padding(all = 8.dp)
//                        .align(Alignment.CenterHorizontally)
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            is ImagenUiState.Success -> {
//                Card(
//                    modifier = Modifier
//                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
//                        .fillMaxWidth(),
//                    shape = MaterialTheme.shapes.large,
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
//                    )
//                ) {
//                    Image(bitmap = uiState.image.asImageBitmap(), "")
//                }
//            }
//
//            is ImagenUiState.Error -> {
//                Card(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .fillMaxWidth(),
//                    shape = MaterialTheme.shapes.large,
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer
//                    )
//                ) {
//                    Text(
//                        text = uiState.errorMessage,
//                        color = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.padding(all = 16.dp)
//                    )
//                }
//            }
//        }
    }
}
