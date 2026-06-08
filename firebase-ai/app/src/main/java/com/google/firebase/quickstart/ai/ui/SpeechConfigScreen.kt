package com.google.firebase.quickstart.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.quickstart.ai.feature.text.SpeechConfigViewModel

@Composable
fun SpeechConfigScreen(viewModel: SpeechConfigViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val latencyMs by viewModel.latencyMs.collectAsState()
    val audioSize by viewModel.audioSize.collectAsState()

    var modelName by remember { mutableStateOf("gemini-2.5-flash-preview-tts") }
    var languageCode by remember { mutableStateOf("cmn") }
    var isMultiSpeaker by remember { mutableStateOf(true) }
    var speaker1Name by remember { mutableStateOf("Speaker1") }
    var speaker1Voice by remember { mutableStateOf("Puck") }
    var speaker2Name by remember { mutableStateOf("Speaker2") }
    var speaker2Voice by remember { mutableStateOf("Charon") }

    var inputText by remember { mutableStateOf("") }

    val presetPromptChinese = """TTS the following conversation between Speaker1 and Speaker2:
Speaker1: 早上好！今天早饭吃什么？
Speaker2: 早上好！我想吃蛋炒饭。你呢？
Speaker1: 我也吃蛋炒饭吧，多加点葱花。"""

    val presetPromptEnglish = """TTS the following conversation between Speaker1 and Speaker2:
Speaker1: Good morning! What's for breakfast today?
Speaker2: Good morning! I'm thinking of making some pancakes.
Speaker1: Sounds delicious! Can you add some blueberries to mine?
Speaker2: Of course! I'll get started right away."""

    val presetPromptSpanish = """TTS the following conversation between Speaker1 and Speaker2:
Speaker1: ¡Buenos días! ¿Qué hay de desayuno hoy?
Speaker2: ¡Buenos días! Estoy pensando en hacer unos huevos revueltos.
Speaker1: ¡Qué rico! ¿Puedes ponerles un poco de queso?
Speaker2: Claro que sí, en un momento estarán listos."""

    val presetPromptJapanese = """TTS the following conversation between Speaker1 and Speaker2:
Speaker1: おはよう！今日の朝御飯は何にする？
Speaker2: おはよう！卵焼きとお味噌汁を作ろうと思っているよ。
Speaker1: 美味しそうだね！ご飯も炊けている？
Speaker2: うん、ちょうど炊けたところだよ。"""

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Configuration Panel (Scrollable Card)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "TTS & Speech Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Model Selection
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Multi-speaker")
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isMultiSpeaker,
                            onCheckedChange = { isMultiSpeaker = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Speaker 1 Config
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = speaker1Name,
                        onValueChange = { speaker1Name = it },
                        label = { Text("Speaker 1 Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = speaker1Voice,
                        onValueChange = { speaker1Voice = it },
                        label = { Text("Speaker 1 Voice") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isMultiSpeaker) {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Speaker 2 Config
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = speaker2Name,
                            onValueChange = { speaker2Name = it },
                            label = { Text("Speaker 2 Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = speaker2Voice,
                            onValueChange = { speaker2Voice = it },
                            label = { Text("Speaker 2 Voice") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Presets Grid
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        inputText = presetPromptChinese
                        languageCode = "cmn"
                        isMultiSpeaker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Chinese Preset")
                }
                Button(
                    onClick = {
                        inputText = presetPromptEnglish
                        languageCode = "en"
                        isMultiSpeaker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("English Preset")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        inputText = presetPromptSpanish
                        languageCode = "es"
                        isMultiSpeaker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Spanish Preset")
                }
                Button(
                    onClick = {
                        inputText = presetPromptJapanese
                        languageCode = "ja"
                        isMultiSpeaker = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Japanese Preset")
                }
            }
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.stopAudio() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop Audio")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.clearConversation() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
            ) {
                Text("Clear")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Status Indicators
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            latencyMs?.let {
                Text("Latency: ${it}ms", style = MaterialTheme.typography.bodySmall)
            }
            audioSize?.let {
                Text("Audio size: ${it} bytes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }

        // Message input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter prompt...") }
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.generateSpeech(
                            prompt = inputText,
                            modelName = modelName,
                            languageCode = languageCode,
                            isMultiSpeaker = isMultiSpeaker,
                            speaker1Name = speaker1Name,
                            speaker1Voice = speaker1Voice,
                            speaker2Name = speaker2Name,
                            speaker2Voice = speaker2Voice
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
