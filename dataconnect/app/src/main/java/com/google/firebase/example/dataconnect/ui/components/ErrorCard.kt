package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.example.dataconnect.R

@Composable
fun ErrorCard(
    errorMessage: String?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = errorMessage ?: stringResource(R.string.unknown_error),
            modifier = Modifier.padding(16.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
@Preview
fun ErrorCardPreview() {
    ErrorCard("Something went terribly wrong :(")
}
