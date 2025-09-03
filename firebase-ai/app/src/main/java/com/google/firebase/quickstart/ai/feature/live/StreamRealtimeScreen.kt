package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.BidiViewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.ImagenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeRoute(val sampleId: String)

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun StreamRealtimeScreen(bidiView: BidiViewModel = viewModel<BidiViewModel>()) {
    val isConversationActive = remember { mutableStateOf(false) }
    val backgroundColor =
        MaterialTheme.colorScheme.background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // The content will animate its size when it changes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.animateContentSize()
            ) {
                if (isConversationActive.value) {
                    // Active state UI
                    Text(
                        text = "Conversation Active",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the end button to stop",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Idle state UI
                    Text(
                        text = "Start Conversation",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the microphone to begin",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // The main button with pulsing animation
            if (isConversationActive.value) {
                // Button to end the conversation
                IconButton(
                    onClick = {
                        bidiView.endConversation()
                        isConversationActive.value = false },
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFFE63946), // A nice red color
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Conversation",
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Button to start the conversation
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            bidiView.startConversation()
                        }
                        isConversationActive.value = true },
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Conversation",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
