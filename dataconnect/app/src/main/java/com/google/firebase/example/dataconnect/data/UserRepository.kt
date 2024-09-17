package com.google.firebase.example.dataconnect.data

import com.google.firebase.dataconnect.movies.GetUserByIdQuery
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance

class UserRepository(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) {

    suspend fun getUserById(userId: String): GetUserByIdQuery.Data.User? {
        return moviesConnector.getUserById.execute(id = userId).data.user
    }

    suspend fun addUser(userName: String) {
        moviesConnector.upsertUser.execute(username = userName)
    }
}