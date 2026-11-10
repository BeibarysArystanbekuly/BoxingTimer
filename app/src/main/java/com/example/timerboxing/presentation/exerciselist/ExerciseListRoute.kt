package com.example.timerboxing.presentation.exerciselist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timerboxing.domain.repository.ExerciseRepository

@Composable
fun ExerciseListRoute(repository: ExerciseRepository, onBack: () -> Unit) {
    val model: ExerciseListViewModel = viewModel(factory = viewModelFactory {
        initializer { ExerciseListViewModel(repository) }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    ExerciseListScreen(state = state, onRetry = model::onRetry, onBack = onBack)
}
