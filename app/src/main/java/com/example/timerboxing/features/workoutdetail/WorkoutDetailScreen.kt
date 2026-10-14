package com.example.timerboxing.features.workoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.timerboxing.R
import com.example.timerboxing.features.workoutdetail.components.TimerControls
import com.example.timerboxing.model.formatDuration
import com.example.timerboxing.model.workoutPresets
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    uiState: WorkoutDetailUiState,
    onBack: () -> Unit,
    onSettings: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onNext: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val workout = uiState.workout
    val isFinished = uiState.phase == WorkoutPhase.FINISHED
    val isRunning = uiState.isRunning
    val hasStarted = uiState.phase != WorkoutPhase.READY
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        workout?.name ?: "Workout not found",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Back to workouts")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(Spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(uiState.error)
                Button(onClick = onRetry) { Text("Retry") }
            } else if (workout == null) {
                Text("This workout is unavailable. Go back and choose another workout.")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large)
                ) {
                    Text(
                        "Work ${formatDuration(workout.workSeconds)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Rest ${formatDuration(workout.restSeconds)}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    when {
                        isFinished -> "Workout complete"
                        uiState.phase == WorkoutPhase.PREPARATION -> "Get ready"
                        uiState.phase == WorkoutPhase.REST -> "Rest"
                        else -> "Work"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    formatDuration(uiState.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge
                )
                Text("Round ${uiState.currentRound}/${workout.rounds}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    when {
                        isFinished -> "Great work! Press play to train again."
                        isRunning -> "Session in progress"
                        hasStarted -> "Paused"
                        else -> "Ready when you are"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                TimerControls(
                    isRunning = isRunning,
                    isPaused = hasStarted && !isRunning && !isFinished,
                    isFinished = isFinished,
                    onPlayPause = onPlayPause,
                    onStop = onStop,
                    onNext = onNext
                )
                Text(
                    "The timer pauses when you leave the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    IconButton(onClick = onSettings) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = "Open workout settings")
                    }
                    Text("Workout settings", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutDetailPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        WorkoutDetailScreen(WorkoutDetailUiState(workout = workoutPresets.first(),
            remainingSeconds = workoutPresets.first().workSeconds, isLoading = false), onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutDetailDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) {
        WorkoutDetailScreen(WorkoutDetailUiState(workout = workoutPresets.first(),
            remainingSeconds = workoutPresets.first().workSeconds, isLoading = false), onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingWorkoutDetailPreview() {
    TImerBoxingTheme { WorkoutDetailScreen(WorkoutDetailUiState(), {}) }
}

@Preview(showBackground = true)
@Composable
fun EmptyWorkoutDetailPreview() {
    TImerBoxingTheme { WorkoutDetailScreen(WorkoutDetailUiState(isLoading = false), {}) }
}

@Preview(showBackground = true)
@Composable
fun ErrorWorkoutDetailPreview() {
    TImerBoxingTheme {
        WorkoutDetailScreen(WorkoutDetailUiState(isLoading = false, error = "Could not load workout."), {})
    }
}
