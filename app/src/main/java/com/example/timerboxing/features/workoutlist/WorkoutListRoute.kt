package com.example.timerboxing.features.workoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timerboxing.data.WorkoutRepository

@Composable
fun WorkoutListRoute(repository: WorkoutRepository, onWorkoutClick: (Int) -> Unit) {
    val model: WorkoutListViewModel = viewModel(factory = viewModelFactory {
        initializer { WorkoutListViewModel(repository) }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    WorkoutListScreen(state, onWorkoutClick, model::onSearchChange, model::onSortChange,
        model::onRetry, model::onSimulateError)
}
