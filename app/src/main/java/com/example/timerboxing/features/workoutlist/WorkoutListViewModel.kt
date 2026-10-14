package com.example.timerboxing.features.workoutlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerboxing.data.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutListViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutListUiState())
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        Log.d("WorkoutListViewModel", "init ${System.identityHashCode(this)}")
        viewModelScope.launch { repository.workouts.collect { updateResults() } }
        onRetry()
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateResults()
    }

    fun onSortChange(sortOrder: WorkoutSortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        updateResults()
    }

    fun onRetry() = load(false)

    fun onSimulateError() = load(true)

    private fun load(simulateError: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.load(simulateError)
                _uiState.update { it.copy(isLoading = false) }
                updateResults()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Could not load workouts.") }
            }
        }
    }

    private fun updateResults() {
        _uiState.update { state ->
            val matches = repository.workouts.value.filter {
                it.name.contains(state.searchQuery.trim(), ignoreCase = true)
            }
            state.copy(workouts = when (state.sortOrder) {
                WorkoutSortOrder.DEFAULT -> matches
                WorkoutSortOrder.NAME -> matches.sortedBy { it.name }
            })
        }
    }

    override fun onCleared() {
        Log.d("WorkoutListViewModel", "onCleared ${System.identityHashCode(this)}")
    }
}
