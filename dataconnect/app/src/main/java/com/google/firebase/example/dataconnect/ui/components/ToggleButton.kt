package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToggleButton(
    iconEnabled: ImageVector,
    iconDisabled: ImageVector,
    textEnabled: String,
    textDisabled: String,
    isEnabled: Boolean,
    onToggle: (newValue: Boolean) -> Unit
) {
    val onClick = {
        onToggle(!isEnabled)
    }
    if (isEnabled) {
        FilledTonalButton(onClick) {
            Icon(iconEnabled, textEnabled)
            Text(textEnabled, modifier = Modifier.padding(horizontal = 4.dp))
        }
    } else {
        OutlinedButton(onClick) {
            Icon(iconDisabled, textDisabled)
            Text(textDisabled, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}
