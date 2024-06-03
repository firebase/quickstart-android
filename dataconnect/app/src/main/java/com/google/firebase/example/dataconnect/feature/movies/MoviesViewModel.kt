package com.google.firebase.example.dataconnect.feature.movies

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.example.dataconnect.data.DataRepository
import com.google.firebase.example.dataconnect.data.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val dataRepository: DataRepository = DataRepository()
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>>
        get() = _movies

    init {
        viewModelScope.launch {
            try {
                _movies.value = dataRepository.listMovies()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}