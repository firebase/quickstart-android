package com.google.firebase.quickstart.database.kotlin.compose.flowcontrol

import java.lang.Exception

sealed class LogInStatus<out T> {
    class Success<out T> : LogInStatus<T>()
    class Failure(val exception: Exception): LogInStatus<Nothing>()
    object InitState : LogInStatus<Nothing>()
}