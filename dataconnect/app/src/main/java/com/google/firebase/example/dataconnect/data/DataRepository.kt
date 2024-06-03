package com.google.firebase.example.dataconnect.data

import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance

class DataRepository(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) {

    suspend fun listMovies(): List<Movie> {
        return moviesConnector.listMovies.execute().data.movies.map { it.toMovie() }
    }
}