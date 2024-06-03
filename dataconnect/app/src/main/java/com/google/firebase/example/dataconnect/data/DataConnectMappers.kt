package com.google.firebase.example.dataconnect.data

import com.google.firebase.dataconnect.movies.ListMoviesQuery

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