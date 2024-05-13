package com.google.firebase.example.dataconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.flow
import com.google.firebase.dataconnect.movies.instance
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    fun fetchMovies() {
        viewModelScope.launch {
            val connector = MoviesConnector.instance

            connector.listMovies.execute()
            connector.createMovie.execute(
                title = "Empire Strikes Back",
                releaseYear = 1980,
                genre = "Sci-Fi",
                rating = 5
            )

            connector.listMoviesByGenre.execute(genre = "Sci-Fi")

//            connector.addMovie.execute(title = "", genre = "")
//
//            val result = connector.listMovies.execute()
//            result.data.movies.firstOrNull()

//            connector.listMoviesGenre.flow(genre = "")
//                .collect { data ->
//                    val movies = data.movies
//                }

            // Connect to the emulator on "10.0.2.2:9510"
            connector.dataConnect.useEmulator()

// (alternatively) if you're running your emulator on non-default port:
            connector.dataConnect.useEmulator(
                port = 9999
            )

        }
    }
}