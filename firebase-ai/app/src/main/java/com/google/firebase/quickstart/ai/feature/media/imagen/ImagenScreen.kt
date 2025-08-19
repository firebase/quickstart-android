package com.google.firebase.quickstart.ai.feature.media.imagen

import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.text.Attachment
import com.google.firebase.quickstart.ai.feature.text.AttachmentsList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class ImagenRoute(val sampleId: String)

@Composable
fun ImagenScreen(
    imagenViewModel: ImagenViewModel = viewModel<ImagenViewModel>()
) {
    var imagenPrompt by rememberSaveable { mutableStateOf(imagenViewModel.initialPrompt) }
    val errorMessage by imagenViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by imagenViewModel.isLoading.collectAsStateWithLifecycle()
    val generatedImages by imagenViewModel.generatedBitmaps.collectAsStateWithLifecycle()
    val includeAttach by imagenViewModel.includeAttach.collectAsStateWithLifecycle()
    val allowEmptyPrompt by imagenViewModel.allowEmptyPrompt.collectAsStateWithLifecycle()
    val attachedImage by imagenViewModel.attachedImage.collectAsStateWithLifecycle()
    val selectionOptions by imagenViewModel.selectionOptions.collectAsStateWithLifecycle()
    val selectedOption by imagenViewModel.selectedOption.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { optionalUri: Uri? ->
        optionalUri?.let { uri ->
            var fileName: String? = null
            // Fetch file name and size
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val humanReadableSize = Formatter.formatShortFileSize(
                    context, cursor.getLong(sizeIndex)
                )
                fileName = "${cursor.getString(nameIndex)} ($humanReadableSize)"
            }

            contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                scope.launch {
                    imagenViewModel.attachImage(bytes)
                }
            }
        }
    }

    Column(
        modifier = Modifier
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
            if (selectionOptions.isNotEmpty()) {
                Column(modifier = Modifier.selectableGroup()) {
                    selectionOptions.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { imagenViewModel.selectRadio(option) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = null // null recommended for accessibility with screen readers
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                }
                }
            }
            if (includeAttach) {
                if (attachedImage != null) {
                    AttachmentsList(listOf(Attachment("", attachedImage)))
                }
            }
            Row() {
                if (includeAttach) {
                    TextButton(
                        onClick = {
                            openDocument.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) { Text("Attach") }
                }
                TextButton(
                    onClick = {
                        if (allowEmptyPrompt || imagenPrompt.isNotBlank()) {
                            imagenViewModel.generateImages(imagenPrompt)
                        }
                    },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(16.dp)
        ) {
            items(generatedImages) { image ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Image(bitmap = image.asImageBitmap(), "Generated image")
                }
            }
        }
    }
}
