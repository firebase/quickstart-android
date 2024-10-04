package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.dataconnect.movies.GetMovieByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.ReviewCard

@Composable
fun UserReviews(
    onReviewSubmitted: (rating: Float, text: String) -> Unit,
    reviews: List<GetMovieByIdQuery.Data.Movie.ReviewsItem>? = emptyList()
) {
    var reviewText by remember { mutableStateOf("") }
    Text(
        text = "User Reviews",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var rating by remember { mutableFloatStateOf(5f) }
        Text("Rating: ${rating}")
        Slider(
            value = rating,
            onValueChange = { rating = Math.round(it).toFloat() },
            steps = 10,
            valueRange = 1f..10f
        )
        TextField(
            value = reviewText,
            onValueChange = { if (it.length <= 280) reviewText = it },
            label = { Text(stringResource(R.string.hint_write_review)) },
            supportingText = {
                Text(
                    "${reviewText.length} / 280",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!reviewText.isNullOrEmpty()) {
                    onReviewSubmitted(rating, reviewText)
                    reviewText = ""
                }
            }
        ) {
            Text(stringResource(R.string.button_submit_review))
        }
    }
    Column {
        // TODO(thatfiredev): Handle cases where the list is too long to display
        reviews.orEmpty().forEach {
            ReviewCard(
                userName = it.user.username,
                date = it.reviewDate,
                rating = it.rating?.toDouble() ?: 0.0,
                text = it.reviewText ?: "",
            )
        }
    }
}
