package com.google.firebase.quickstart.ai.feature.text

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.vertexai.type.Content
import com.google.firebase.vertexai.type.TextPart
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class ChatRoute(val sampleId: String)

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel<ChatViewModel>()
) {
    val messages: List<Content> by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading: Boolean by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage: String? by chatViewModel.errorMessage.collectAsStateWithLifecycle()

    val initialPrompt: String = chatViewModel.initialPrompt

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ChatList(
            messages,
            listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)
        )
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                errorMessage?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                val contentResolver = LocalContext.current.contentResolver
                MessageInput(
                    initialPrompt = initialPrompt,
                    onSendMessage = { inputText ->
                        chatViewModel.sendMessage(inputText)
                    },
                    resetScroll = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    },
                    onFileAttached = { uri ->
                        val mimeType = contentResolver.getType(uri).orEmpty()

                        val inputStream = contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val bytes = inputStream.readBytes()
                            chatViewModel.attachFile(bytes, mimeType)
                        } else {
                            // Unable to read file - show an error
                        }
                        inputStream?.close()
                        Log.d("ChatScreen", "path: ${uri.toString()}")
                        Log.d("ChatScreen", "mimeType: $mimeType")
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    chatMessage: Content
) {
    val isModelMessage = chatMessage.role == "model"

    val backgroundColor = when (chatMessage.role) {
        "user" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = if (isModelMessage) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = chatMessage.role?.uppercase() ?: "USER",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row {
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    Text(
                        text = chatMessage.parts.filterIsInstance<TextPart>().joinToString(" ") { it.text },
                        modifier = Modifier.padding(16.dp),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ChatList(
    chatMessages: List<Content>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = listState,
        modifier = modifier
    ) {
        items(chatMessages.reversed()) { message ->
            ChatBubbleItem(message)
        }
    }
}

@Composable
fun MessageInput(
    initialPrompt: String,
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {},
    onFileAttached: (Uri) -> Unit,
    isLoading: Boolean = false
) {
    var userMessage by rememberSaveable { mutableStateOf(initialPrompt) }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = userMessage,
            label = { Text("Message") },
            onValueChange = { userMessage = it },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 4.dp)
                .fillMaxWidth()
                .weight(1f)
        )
        AttachmentsMenu(
            modifier = Modifier.align(Alignment.CenterVertically),
            onFileAttached = onFileAttached
        )
        IconButton(
            onClick = {
                if (userMessage.isNotBlank()) {
                    onSendMessage(userMessage)
                    userMessage = ""
                    resetScroll()
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .background(
                    color = if (isLoading) {
                        IconButtonDefaults.iconButtonColors().disabledContainerColor
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
        ) {
            Icon(
                Icons.AutoMirrored.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}


@Composable
fun AttachmentsMenu(
    modifier: Modifier = Modifier,
    onFileAttached: (Uri) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            onFileAttached(it)
        }
    }
    Box(
        modifier = modifier
            .padding(end = 4.dp)
    ) {
        IconButton(
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                Icons.Default.AttachFile,
                contentDescription = "Attach",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Text(
                text = "Attach",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            DropdownMenuItem(
                text = { Text("Image / Video") },
                onClick = {
                    openDocument.launch(arrayOf("image/*", "video/*"))
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Audio") },
                onClick = {
                    openDocument.launch(arrayOf("audio/*"))
                    expanded = !expanded
                }
            )
            DropdownMenuItem(
                text = { Text("Document (PDF)") },
                onClick = {
                    openDocument.launch(arrayOf("application/pdf"))
                    expanded = !expanded
                }
            )
        }
    }
}
