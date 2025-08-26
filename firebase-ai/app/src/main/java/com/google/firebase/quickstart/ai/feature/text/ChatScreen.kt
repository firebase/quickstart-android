package com.google.firebase.quickstart.ai.feature.text

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.firebase.ai.type.FileDataPart
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.WebGroundingChunk
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class ChatRoute(val sampleId: String)

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel<ChatViewModel>()
) {
    val messages: List<UiChatMessage> by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading: Boolean by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage: String? by chatViewModel.errorMessage.collectAsStateWithLifecycle()
    val attachments: List<Attachment> by chatViewModel.attachments.collectAsStateWithLifecycle()

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
                AttachmentsList(attachments)
                val context = LocalContext.current
                val contentResolver = context.contentResolver
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
                        var fileName: String? = null
                        // Fetch file name and size
                        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            cursor.moveToFirst()
                            val humanReadableSize = Formatter.formatShortFileSize(
                                context,
                                cursor.getLong(sizeIndex)
                            )
                            fileName = "${cursor.getString(nameIndex)} ($humanReadableSize)"
                        }

                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bytes = stream.readBytes()
                            chatViewModel.attachFile(bytes, mimeType, fileName)
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChatBubbleItem(
    message: UiChatMessage
) {
    val isModelMessage = message.content.role == "model"

    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = when (message.content.role) {
        "user" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = if (isModelMessage) {
        MaterialTheme.colorScheme.onBackground
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
            text = message.content.role?.uppercase() ?: "USER",
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
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        message.content.parts.forEach { part ->
                            when (part) {
                                is TextPart -> {
                                    Text(
                                        text = part.text.trimIndent(),
                                        modifier = Modifier.fillMaxWidth(),
                                        color = textColor
                                    )
                                }

                                is ImagePart -> {
                                    Image(
                                        bitmap = part.image.asImageBitmap(),
                                        contentDescription = "Attached image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    )
                                }

                                is InlineDataPart -> {
                                    // TODO: show a human readable version of audio, PDFs and videos
                                    val attachmentType = if (part.mimeType.contains("audio")) {
                                        "audio attached"
                                    } else if (part.mimeType.contains("application/pdf")) {
                                        "PDF attached"
                                    } else if (part.mimeType.contains("video")) {
                                        "video"
                                    } else {
                                        "file attached"
                                    }
                                    Text(
                                        text = "($attachmentType)",
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.End
                                    )
                                }

                                is FileDataPart -> {
                                    Text(
                                        text = part.uri,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier
                                            .background(
                                                backgroundColor.copy(
                                                    red = backgroundColor.red * 0.7f,
                                                    green = backgroundColor.green * 0.7f,
                                                    blue = backgroundColor.blue * 0.7f
                                                )
                                            )
                                            .padding(4.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                        message.groundingMetadata?.let { metadata ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

                            // Search Entry Point (WebView)
                            metadata.searchEntryPoint?.let { searchEntryPoint ->
                                val context = LocalContext.current
                                AndroidView(
                                    factory = {
                                        WebView(it).apply {
                                            webViewClient = object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(
                                                    view: WebView?,
                                                    request: WebResourceRequest?
                                                ): Boolean {
                                                    request?.url?.let { uri ->
                                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                                        context.startActivity(intent)
                                                    }
                                                    // Return true to indicate we handled the URL loading
                                                    return true
                                                }
                                            }

                                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                            loadDataWithBaseURL(
                                                null,
                                                searchEntryPoint.renderedContent,
                                                "text/html",
                                                "UTF-8",
                                                null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(22.dp))
                                        .fillMaxHeight()
                                        .fillMaxWidth()
                                )
                            }

                            if (metadata.groundingChunks.isNotEmpty()) {
                                Text(
                                    text = "Sources",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                                metadata.groundingChunks.forEach { chunk ->
                                    chunk.web?.let { SourceLinkView(it) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceLinkView(
    webChunk: WebGroundingChunk
) {
    val context = LocalContext.current
    val annotatedString = AnnotatedString.Builder(webChunk.title ?: "Untitled Source").apply {
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            ),
            start = 0,
            end = webChunk.title?.length ?: "Untitled Source".length
        )
        webChunk.uri?.let { addStringAnnotation("URL", it, 0, it.length) }
    }.toAnnotatedString()

    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(
            Icons.Default.Attachment,
            contentDescription = "Source link",
            modifier = Modifier.padding(end = 8.dp)
        )
        ClickableText(text = annotatedString, onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)))
                }
        })
    }
}

@Composable
fun ChatList(
    chatMessages: List<UiChatMessage>,
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

/**
 * Meant to present attachments in the UI
 */
data class Attachment(
    val fileName: String,
    val image: Bitmap? = null // only for image attachments
)

@Composable
fun AttachmentsList(
    attachments: List<Attachment>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        attachments.forEach { attachment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Attachment,
                    contentDescription = "Attachment icon",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(modifier = Modifier.align (Alignment.CenterVertically)) {
                    attachment.image?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = attachment.fileName,
                            modifier = Modifier
                        )
                    }
                    Text(
                        text = attachment.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}