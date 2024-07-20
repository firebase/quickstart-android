package com.google.firebase.quickstart.database.kotlin.compose.flowcontrol

import java.lang.Exception

sealed class LoadDataStatus<out T> {
    class Loaded<out T>(): LoadDataStatus<T>()
    class Failed(val exception: Exception): LoadDataStatus<Nothing>()
    object Loading : LoadDataStatus<Nothing>()
}