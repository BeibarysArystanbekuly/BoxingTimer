package com.example.timerboxing.presentation.exerciselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerboxing.domain.model.Exercise
import com.example.timerboxing.domain.repository.ExerciseRepository
import com.example.timerboxing.presentation.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseListViewModel(private val repository: ExerciseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Exercise>>(UiState.Loading)
    val uiState: StateFlow<UiState<Exercise>> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        onRetry()
    }

    fun onRetry() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val items = repository.getItems()
                _uiState.value = UiState.Success(items)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(exception.message ?: "Could not load exercises.")
            }
        }
    }
}
