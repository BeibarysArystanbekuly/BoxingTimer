package com.example.timerboxing.features.workoutsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timerboxing.data.WorkoutRepository

@Composable
fun WorkoutSettingsRoute(repository: WorkoutRepository, onBack: () -> Unit, onSaved: (Int) -> Unit) {
    val model: WorkoutSettingsViewModel = viewModel(factory = viewModelFactory {
        initializer { WorkoutSettingsViewModel(repository, createSavedStateHandle()) }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(model, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            model.savedEvents.collect { workoutId ->
                if (workoutId != null && model.consumeSavedEvent(workoutId)) currentOnSaved(workoutId)
            }
        }
    }
    WorkoutSettingsScreen(state, onBack, model::onSave, model::onFieldChange,
        model::onRestWarningChange, model::onRetry)
}
