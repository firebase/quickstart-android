package com.google.firebase.example.dataconnect.ui.components

import android.os.Build
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.dataconnect.LocalDate
import com.google.firebase.dataconnect.toJavaLocalDate
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun ReviewCard(
    userName: String,
    date: LocalDate,
    rating: Double,
    text: String,
    movieName: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp)
        ) {
            Text(
                text = if (movieName != null) {
                    userName + " on " + movieName
                } else {
                    userName
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault())
                            date.toJavaLocalDate().format(dateFormatter)
                        } else {
                            val parseableDateString = date.run {
                                val year = "$year".padStart(4, '0')
                                val month = "$month".padStart(2, '0')
                                val day = "$day".padStart(2, '0')
                                "$year-$month-$day"
                            }
                            val dateParser = SimpleDateFormat("y-M-d", Locale.US)
                            val parsedDate = dateParser.parse(parseableDateString) ?:
                              throw Exception("INTERNAL ERROR: unparseable date string: $parseableDateString")
                            val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                            dateFormatter.format(parsedDate)
                        },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rating: ",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$rating",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
