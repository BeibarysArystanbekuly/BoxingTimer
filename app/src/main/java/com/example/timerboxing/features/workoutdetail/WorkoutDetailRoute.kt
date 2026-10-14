package com.example.timerboxing.features.workoutdetail

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.timerboxing.data.WorkoutRepository
import com.example.timerboxing.features.workoutdetail.platform.WorkoutLifecycleObserver
import com.example.timerboxing.features.workoutdetail.platform.rememberWorkoutSignal

@Composable
fun WorkoutDetailRoute(repository: WorkoutRepository, onBack: () -> Unit, onSettings: () -> Unit) {
    val model: WorkoutDetailViewModel = viewModel(factory = viewModelFactory {
        initializer { WorkoutDetailViewModel(repository, createSavedStateHandle()) }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = remember(context) {
        generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull()
    }
    val view = LocalView.current
    val hostLifecycleOwner = activity as? androidx.lifecycle.LifecycleOwner ?: lifecycleOwner
    val playSignal = rememberWorkoutSignal()
    DisposableEffect(view, state.isRunning) {
        val previous = view.keepScreenOn
        if (state.isRunning) view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
    DisposableEffect(hostLifecycleOwner, activity, model) {
        val observer = WorkoutLifecycleObserver(
            isChangingConfigurations = { activity?.isChangingConfigurations == true },
            onBackground = model::onPause
        )
        hostLifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            hostLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(model, lifecycleOwner, playSignal) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            model.signals.collect { event ->
                if (event != null && model.consumeSignal(event)) playSignal()
            }
        }
    }
    val navigateBack = { model.onPause(); onBack() }
    BackHandler(onBack = navigateBack)
    WorkoutDetailScreen(state, navigateBack, { model.onPause(); onSettings() },
        model::onPlayPause, model::onStop, model::onNext, model::onRetry)
}
