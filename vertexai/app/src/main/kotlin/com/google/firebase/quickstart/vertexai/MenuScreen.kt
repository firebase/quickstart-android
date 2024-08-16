/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.vertexai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class MenuItem(
    val routeId: String,
    val titleResId: Int,
    val descriptionResId: Int
)

@Composable
fun MenuScreen(
    onItemClicked: (String) -> Unit = { }
) {
    val menuItems = listOf(
        MenuItem("summarize", R.string.menu_summarize_title, R.string.menu_summarize_description),
        MenuItem("photo_reasoning", R.string.menu_reason_title, R.string.menu_reason_description),
        MenuItem("chat", R.string.menu_chat_title, R.string.menu_chat_description),
        MenuItem("functions_chat", R.string.menu_functions_title, R.string.menu_functions_description),
        MenuItem("audio", R.string.menu_audio_title, R.string.menu_audio_description)
    )

    LazyColumn(
        Modifier
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        items(menuItems) { menuItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(menuItem.titleResId),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(menuItem.descriptionResId),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            onItemClicked(menuItem.routeId)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.action_try))
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    MenuScreen()
}
