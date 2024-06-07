package com.google.firebase.example.dataconnect.data

import com.google.firebase.dataconnect.movies.GetMovieByIdQuery
import com.google.firebase.dataconnect.movies.ListMoviesByGenreQuery
import com.google.firebase.dataconnect.movies.ListMoviesQuery
import com.google.firebase.dataconnect.movies.MoviesRecentlyReleasedQuery
import com.google.firebase.dataconnect.movies.MoviesTop10Query

fun ListMoviesQuery.Data.MoviesItem.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        genre = this.genre,
        imageUrl = this.imageUrl,
        releaseYear = this.releaseYear,
        rating = this.rating,
        tags = this.tags
    )
}

fun MoviesTop10Query.Data.MoviesItem.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        rating = this.rating,
        genre = this.genre,
        tags = this.tags
    )
}

fun MoviesRecentlyReleasedQuery.Data.MoviesItem.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        rating = this.rating,
        genre = this.genre,
        tags = this.tags
    )
}

fun ListMoviesByGenreQuery.Data.MostRecentItem.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        rating = this.rating,
        tags = this.tags
    )
}

fun ListMoviesByGenreQuery.Data.MostPopularItem.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        rating = this.rating,
        tags = this.tags
    )
}

fun GetMovieByIdQuery.Data.Movie.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        rating = this.rating,
        genre = this.genre,
        tags = this.tags
    )
}
