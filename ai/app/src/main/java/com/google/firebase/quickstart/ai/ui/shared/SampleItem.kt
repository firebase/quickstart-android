package com.google.firebase.quickstart.ai.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SampleItem(
    titleResId: String,
    descriptionResId: String,
    onItemClicked: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .padding(4.dp)
            .clickable {
                onItemClicked()
            }
    ) {
        Column(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxSize()
        ) {
            Text(
//                text = stringResource(menuItem.titleResId),
                text = titleResId,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
//                text = stringResource(menuItem.descriptionResId),
                text = descriptionResId,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}