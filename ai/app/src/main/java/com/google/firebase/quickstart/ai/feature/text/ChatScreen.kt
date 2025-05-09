package com.google.firebase.quickstart.ai.feature.text

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.vertexai.type.Content
import com.google.firebase.vertexai.type.asTextOrNull
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class Participant {
    USER, MODEL, ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.USER,
) {
    constructor(content: Content) : this(
        text = content.parts.first().asTextOrNull() ?: "",
        participant = if (content.role == "user") Participant.USER else Participant.MODEL
    )
}

@Serializable
class ChatRoute(val sampleId: String)

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel<ChatViewModel>()
) {
    val messages: List<ChatMessage> by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading: Boolean by chatViewModel.isLoading.collectAsStateWithLifecycle()
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
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                MessageInput(
                    initialPrompt = initialPrompt,
                    onSendMessage = { inputText ->
                        chatViewModel.sendMessage(inputText)
                    },
                    resetScroll = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    chatMessage: ChatMessage
) {
    val isModelMessage = chatMessage.participant == Participant.MODEL ||
            chatMessage.participant == Participant.ERROR

    val backgroundColor = when (chatMessage.participant) {
        Participant.MODEL -> MaterialTheme.colorScheme.primaryContainer
        Participant.USER -> MaterialTheme.colorScheme.tertiaryContainer
        Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
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
            text = chatMessage.participant.name,
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
                        text = chatMessage.text,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatList(
    chatMessages: List<ChatMessage>,
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
    resetScroll: () -> Unit = {}
) {
    var userMessage by rememberSaveable { mutableStateOf(initialPrompt) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
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
                    .fillMaxWidth()
                    .weight(0.85f)
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                        resetScroll()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier
                )
            }
        }
    }
}
