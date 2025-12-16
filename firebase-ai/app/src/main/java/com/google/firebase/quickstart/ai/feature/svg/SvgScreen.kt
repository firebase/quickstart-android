package com.google.firebase.quickstart.ai.feature.svg

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer

@Serializable
class SvgRoute(val sampleId: String)

@Composable
fun SvgScreen(
    svgViewModel: SvgViewModel = viewModel<SvgViewModel>()
) {
    var prompt by rememberSaveable { mutableStateOf(svgViewModel.initialPrompt) }
    val errorMessage by svgViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by svgViewModel.isLoading.collectAsStateWithLifecycle()
    val generatedSvgs by svgViewModel.generatedSvgs.collectAsStateWithLifecycle()

    Column {
        ElevatedCard(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            OutlinedTextField(
                value = prompt,
                label = { Text("Generate a SVG of") },
                placeholder = { Text("Enter text to generate image") },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            TextButton(
                onClick = {
                    svgViewModel.generateSVG(prompt)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .align(Alignment.End)
            ) {
                Text("Generate")
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
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(generatedSvgs) { svg ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ByteBuffer.wrap(svg.toByteArray()))
                            .decoderFactory(SvgDecoder.Factory())
                            .decoderCoroutineContext(Dispatchers.Main)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated SVG",
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
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
    }
}