package com.google.firebase.example.dataconnect.data

import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import java.util.UUID

class MovieRepository(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) {

    // Queries
    suspend fun listMovies(): List<Movie> {
        return moviesConnector.listMovies.execute().data.movies.map { it.toMovie() }
    }

    suspend fun getTop10Movies(): List<Movie> {
        return moviesConnector.moviesTop10.execute().data.movies.map { it.toMovie() }
    }

    suspend fun getRecentlyReleasedMovies(): List<Movie> {
        return moviesConnector.moviesRecentlyReleased.execute().data.movies.map { it.toMovie() }
    }

    suspend fun getMoviesByGenre(genre: String): MoviesByGenre {
        val data = moviesConnector.listMoviesByGenre.execute(genre).data
        val mostPopular = data.mostPopular.map { it.toMovie() }
        val mostRecent = data.mostRecent.map { it.toMovie() }
        return MoviesByGenre(mostPopular, mostRecent)
    }

    suspend fun getMovieByID(movieID: String): Movie? {
        val id = UUID.fromString(movieID)
        return moviesConnector.getMovieById.execute(id).data.movie?.toMovie()
    }

    // Mutations
    suspend fun addMovieToFavorites(movieID: String) {
        moviesConnector.addFavoritedMovie.execute(UUID.fromString(movieID))
    }
}